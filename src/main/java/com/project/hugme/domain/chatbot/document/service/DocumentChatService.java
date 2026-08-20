package com.project.hugme.domain.chatbot.document.service;

import com.project.hugme.domain.chatbot.document.dto.DocumentChatResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentSourceResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchRequest;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentPreparationDocumentResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentPreparationResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentPreparationSectionResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentPreparationUpdateRequest;
import com.project.hugme.domain.chatbot.document.entity.DocumentPreparationCheck;
import com.project.hugme.domain.chatbot.document.entity.DocumentChatHistory;
import com.project.hugme.domain.chatbot.document.repository.DocumentPreparationCheckRepository;
import com.project.hugme.domain.chatbot.document.repository.DocumentChatHistoryRepository;
import com.project.hugme.domain.checklist.entity.application.ChecklistDocumentResult;
import com.project.hugme.domain.checklist.entity.product.ChecklistSection;
import com.project.hugme.domain.checklist.entity.product.Document;
import com.project.hugme.domain.checklist.entity.product.SectionCode;
import com.project.hugme.domain.checklist.repository.ChecklistDocumentResultRepository;
import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.repository.ApplicationRepository;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentChatService {

    private static final int MAX_DOCUMENTS_IN_ANSWER = 5;

    private final DocumentSearchFacade documentSearchFacade;
    private final ChatClient.Builder chatClientBuilder;
    private final ChecklistDocumentResultRepository checklistDocumentResultRepository;
    private final ApplicationRepository applicationRepository;
    private final DocumentPreparationCheckRepository preparationCheckRepository;
    private final DocumentChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DocumentPreparationResponse getPreparationStatus(Long userId) {
        Application application = getApplicationByUserId(userId);
        Long applicationId = application.getApplicationId();
        List<ChecklistDocumentResult> results =
                checklistDocumentResultRepository.findCurrentDocuments(applicationId, userId);
        Set<Long> checkedDocumentIds = preparationCheckRepository.findCheckedDocumentIds(applicationId);
        Map<SectionCode, List<DocumentPreparationDocumentResponse>> documentsBySection =
                new EnumMap<>(SectionCode.class);
        Map<SectionCode, String> sectionNames = new EnumMap<>(SectionCode.class);

        for (ChecklistDocumentResult result : results) {
            Document document = result.getDocument();
            ChecklistSection section = document.getItem().getSection();
            sectionNames.putIfAbsent(section.getSectionCode(), section.getSectionName());
            documentsBySection.computeIfAbsent(section.getSectionCode(), ignored -> new ArrayList<>())
                    .add(DocumentPreparationDocumentResponse.from(
                            document, checkedDocumentIds.contains(document.getDocumentId())));
        }

        List<DocumentPreparationSectionResponse> sections = new ArrayList<>();
        for (SectionCode sectionCode : SectionCode.values()) {
            List<DocumentPreparationDocumentResponse> documents = documentsBySection.get(sectionCode);
            if (documents != null && !documents.isEmpty()) {
                sections.add(new DocumentPreparationSectionResponse(
                        sectionCode, sectionNames.get(sectionCode), documents));
            }
        }

        int preparedCount = (int) results.stream()
                .map(result -> result.getDocument().getDocumentId())
                .filter(checkedDocumentIds::contains)
                .count();
        return new DocumentPreparationResponse(applicationId, results.size(), preparedCount, sections);
    }

    @Transactional
    public DocumentPreparationResponse updatePreparationStatus(
            Long userId,
            Long documentId,
            DocumentPreparationUpdateRequest request
    ) {
        Application application = getApplicationByUserId(userId);
        Long applicationId = application.getApplicationId();
        ChecklistDocumentResult result = checklistDocumentResultRepository
                .findCurrentDocuments(applicationId, userId).stream()
                .filter(item -> item.getDocument().getDocumentId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("준비 서류를 찾을 수 없습니다."));

        if (request.prepared()) {
            if (!preparationCheckRepository.findCheckedDocumentIds(applicationId).contains(documentId)) {
                preparationCheckRepository.save(DocumentPreparationCheck.create(
                        result.getApplication(), result.getDocument()));
            }
        } else {
            preparationCheckRepository.deleteByApplicationApplicationIdAndDocumentDocumentId(
                    applicationId, documentId);
        }
        return getPreparationStatus(userId);
    }

    private Application getApplicationByUserId(Long userId) {
        return applicationRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));
    }

    public DocumentChatResponse chat(
            Long userId,
            DocumentSearchRequest request
    ) throws Exception {

        long totalStartNanos = System.nanoTime();
        try {

        List<DocumentSearchResponse> searchResults =
                documentSearchFacade.search(request);

        if (searchResults.isEmpty()) {
            DocumentChatResponse response = new DocumentChatResponse(
                    "해당 질문에 대해 안내할 수 있는 서류 정보를 찾지 못했습니다.",
                    List.of()
            );
            saveChatHistory(userId, request, response);
            return response;
        }

        List<DocumentSearchResponse> answerResults = searchResults.stream()
                .limit(MAX_DOCUMENTS_IN_ANSWER)
                .toList();

        String context = buildContext(answerResults, searchResults.size());

        ChatClient chatClient = chatClientBuilder.build();

        long answerLlmStartNanos = System.nanoTime();
        String answer = chatClient
                .prompt()
                .system("""
                        여러 서류 정보가 제공되면, 제공된 모든 서류명을 빠뜨리지 말고 목록으로 안내하세요.
                        검색 결과에 없는 서류는 추가하지 마세요.
                        검색 결과가 일부만 제공된 경우에는 전체 건수와 "대표 서류"임을 함께 안내하세요.

                        답변 본문에는 URL, Markdown 링크, "여기" 또는 "이곳" 같은 링크 안내 문구를 넣지 마세요.
                        출처 URL은 별도의 sources 데이터로 전달되므로, 본문에서는 출처를 언급할 필요가 없습니다.

                        당신은 HUG 전세보증 관련 서류 안내 챗봇입니다.

                        반드시 아래 규칙을 지켜 답변하세요.

                        1. 제공된 [서류 정보]만 근거로 답변하세요.
                        2. 제공되지 않은 내용을 추측하거나 만들어내지 마세요.
                        3. 사용자의 질문에 필요한 내용만 간결하고 이해하기 쉽게 답변하세요.
                        4. AVAILABLE은 '가능', UNAVAILABLE은 '불가능',
                           CONDITIONAL은 '조건부 가능',
                           UNKNOWN은 '확인 필요'의 의미로 자연스럽게 설명하세요.
                        5. 정보가 없거나 NULL인 항목은 임의로 보완하지 마세요.
                        6. 정확한 정보가 없는 경우
                           '현재 확인된 정보만으로는 안내하기 어렵습니다.'라고 안내하세요.
                        7. URL은 제공된 공식 안내 URL 또는 발급 URL만 사용하세요.
                        8. 검색 점수나 Elasticsearch, BM25, Vector Search 같은
                           내부 구현 정보는 사용자에게 설명하지 마세요.
                        """)
                .user("""
                        [사용자 질문]
                        %s

                        [서류 정보]
                        %s

                        위 정보만 사용해서 사용자 질문에 답변하세요.
                        """.formatted(
                        request.question(),
                        context
                ))
                .call()
                .content();
        log.info("[PERF][document-rag] stage=answer_llm duration_ms={}",
                elapsedMillis(answerLlmStartNanos));

        List<DocumentSourceResponse> sources = extractSources(answerResults);

        DocumentChatResponse response = new DocumentChatResponse(
                answer,
                sources
        );
        saveChatHistory(userId, request, response);
        return response;
        } finally {
            log.info("[PERF][document-rag] stage=total duration_ms={}",
                    elapsedMillis(totalStartNanos));
        }
    }

    private void saveChatHistory(
            Long userId,
            DocumentSearchRequest request,
            DocumentChatResponse response
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        chatHistoryRepository.save(DocumentChatHistory.create(
                user,
                request.documentId(),
                request.question(),
                response.answer(),
                response.sources().stream()
                        .map(DocumentSourceResponse::url)
                        .collect(Collectors.joining("\n"))
        ));
    }

    private String buildContext(
            List<DocumentSearchResponse> searchResults,
            int totalResultCount
    ) {

        StringBuilder context = new StringBuilder();

        if (totalResultCount > searchResults.size()) {
            context.append("검색 결과는 총 ")
                    .append(totalResultCount)
                    .append("건이며, 답변에는 대표 ")
                    .append(searchResults.size())
                    .append("건만 제공합니다.\n\n");
        }

        for (DocumentSearchResponse result : searchResults) {

            context.append("서류명: ")
                    .append(result.documentName())
                    .append("\n");

            if (result.fields() != null) {
                result.fields().forEach((key, value) -> {
                    if (value != null) {
                        context.append(key)
                                .append(": ")
                                .append(value)
                                .append("\n");
                    }
                });
            }

            if (result.officialGuideUrl() != null) {
                context.append("공식 안내 URL: ")
                        .append(result.officialGuideUrl())
                        .append("\n");
            }

            if (result.hugReferenceUrls() != null
                    && !result.hugReferenceUrls().isEmpty()) {

                context.append("HUG 근거 URL: ")
                        .append(
                                String.join(
                                        ", ",
                                        result.hugReferenceUrls()
                                )
                        )
                        .append("\n");
            }

            context.append("\n");
        }

        return context.toString();
    }

    private List<DocumentSourceResponse> extractSources(
            List<DocumentSearchResponse> searchResults
    ) {

        Map<String, String> titlesByUrl = new LinkedHashMap<>();

        for (DocumentSearchResponse result : searchResults) {

            if (result.officialGuideUrl() != null
                    && !result.officialGuideUrl().isBlank()) {

                titlesByUrl.putIfAbsent(
                        result.officialGuideUrl(),
                        getOfficialSourceTitle(result.officialGuideUrl())
                );
            }

            if (result.hugReferenceUrls() != null) {

                for (String url : result.hugReferenceUrls()) {

                    if (url != null && !url.isBlank()) {
                        titlesByUrl.putIfAbsent(url, getHugSourceTitle(url));
                    }
                }
            }
        }

        return titlesByUrl.entrySet().stream()
                .map(entry -> new DocumentSourceResponse(entry.getValue(), entry.getKey()))
                .toList();
    }

    private String getOfficialSourceTitle(String url) {
        String host = getHost(url);
        if ("gov.kr".equals(host) || "www.gov.kr".equals(host)) {
            return "정부24 발급 안내";
        }
        return "공식 안내";
    }

    private String getHugSourceTitle(String url) {
        String host = getHost(url);
        if ("m.khug.or.kr".equals(host)) {
            return "HUG 보증 안내";
        }
        if ("khug.or.kr".equals(host) || "www.khug.or.kr".equals(host)) {
            return "HUG 서류 안내";
        }
        return "HUG 안내";
    }

    private String getHost(String url) {
        try {
            return URI.create(url).getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}

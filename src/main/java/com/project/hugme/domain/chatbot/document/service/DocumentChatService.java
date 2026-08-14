package com.project.hugme.domain.chatbot.document.service;

import com.project.hugme.domain.chatbot.document.dto.DocumentChatResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchRequest;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocumentChatService {

    private final DocumentSearchFacade documentSearchFacade;
    private final ChatClient.Builder chatClientBuilder;

    public DocumentChatResponse chat(
            DocumentSearchRequest request
    ) throws Exception {

        List<DocumentSearchResponse> searchResults =
                documentSearchFacade.search(request);

        if (searchResults.isEmpty()) {
            return new DocumentChatResponse(
                    "해당 질문에 대해 안내할 수 있는 서류 정보를 찾지 못했습니다.",
                    List.of()
            );
        }

        String context = buildContext(searchResults);

        ChatClient chatClient = chatClientBuilder.build();

        String answer = chatClient
                .prompt()
                .system("""
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

        List<String> sources = extractSources(searchResults);

        return new DocumentChatResponse(
                answer,
                sources
        );
    }

    private String buildContext(
            List<DocumentSearchResponse> searchResults
    ) {

        StringBuilder context = new StringBuilder();

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

    private List<String> extractSources(
            List<DocumentSearchResponse> searchResults
    ) {

        Set<String> sources = new LinkedHashSet<>();

        for (DocumentSearchResponse result : searchResults) {

            if (result.officialGuideUrl() != null
                    && !result.officialGuideUrl().isBlank()) {

                sources.add(result.officialGuideUrl());
            }

            if (result.hugReferenceUrls() != null) {

                for (String url : result.hugReferenceUrls()) {

                    if (url != null && !url.isBlank()) {
                        sources.add(url);
                    }
                }
            }
        }

        return List.copyOf(sources);
    }
}
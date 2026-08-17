package com.project.hugme.domain.chatbot.guide.service;

import com.project.hugme.domain.chatbot.FeatureType;
import com.project.hugme.domain.chatbot.guide.dto.ChatRequest;
import com.project.hugme.domain.chatbot.guide.dto.ChatResponse;
import com.project.hugme.domain.chatbot.guide.dto.RedirectDto;
import com.project.hugme.domain.chatbot.guide.dto.SourceDto;
import com.project.hugme.domain.chatbot.guide.entity.GuideChatHistory;
import com.project.hugme.domain.chatbot.guide.repository.GuideChatHistoryRepository;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuideChatService {

    private final IntentClassificationService intentClassificationService;
    private final HybridSearchService hybridSearchService;
    private final AnswerGenerationService answerGenerationService;
    private final SuggestedQuestionService suggestedQuestionService;
    private final MetaAnswerService metaAnswerService;
    private final GuideChatHistoryRepository guideChatHistoryRepository;
    private final UserRepository userRepository;

    public ChatResponse handle(Long userId, ChatRequest request) {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();

        var route = intentClassificationService.route(request.message());

        if ("feature".equals(route.category())) {
            if (route.featureType() == null) {
                ChatResponse response = new ChatResponse(
                        sessionId,
                        "어떤 기능을 원하시는지 정확히 파악하지 못했어요. 조금 더 구체적으로 말씀해주시겠어요?",
                        route.category(), List.of(), List.of(), null
                );
                saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
                return response;
            }
            FeatureType feature = route.featureType();
            String answer = feature.description() + "이에요. " + feature.label() + "으로 이동하시겠어요?";
            RedirectDto redirect = new RedirectDto(feature.name(), feature.label(), feature.path());
            ChatResponse response = new ChatResponse(
                    sessionId, answer, route.category(), List.of(), List.of(), redirect
            );
            saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
            return response;
        }

        if ("meta".equals(route.category())) {
            String answer = metaAnswerService.generate(sessionId, request.message());
            ChatResponse response = new ChatResponse(sessionId, answer, route.category(), List.of(), List.of(), null);
            saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
            return response;
        }

        if ("off_topic".equals(route.category())) {
            ChatResponse response = new ChatResponse(
                    sessionId,
                    "죄송해요, 저는 상담과 관련된 내용만 도와드릴 수 있어요.",
                    route.category(), List.of(), List.of(), null
            );
            saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
            return response;
        }

        List<Document> results = hybridSearchService.search(request.message(), route.sources());
        String answer = answerGenerationService.generate(sessionId, request.message(), results);

        List<SourceDto> sources = results.stream()
                .map(doc -> new SourceDto(
                        String.valueOf(doc.getMetadata().getOrDefault("source", "unknown")),
                        doc.getText().length() > 100 ? doc.getText().substring(0, 100) + "..." : doc.getText()
                ))
                .toList();

        List<String> suggestedQuestions = suggestedQuestionService.suggest(request.message(), answer);

        ChatResponse response = new ChatResponse(sessionId, answer, route.category(), sources, suggestedQuestions, null);
        saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
        return response;
    }

    private void saveHistoryIfLoggedIn(Long userId, String sessionId, String question, ChatResponse response) {
        if (userId == null) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String sourcesJoined = response.sources().stream()
                .map(SourceDto::source)
                .collect(Collectors.joining("\n"));

        guideChatHistoryRepository.save(GuideChatHistory.create(
                user, sessionId, response.category(), question, response.answer(), sourcesJoined
        ));
    }
}
package com.project.hugme.domain.chatbot.guide.service;

import com.project.hugme.domain.chatbot.FeatureType;
import com.project.hugme.domain.chatbot.guide.dto.ChatRequest;
import com.project.hugme.domain.chatbot.guide.dto.ChatResponse;
import com.project.hugme.domain.chatbot.guide.dto.RedirectDto;
import com.project.hugme.domain.chatbot.guide.dto.SourceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuideChatService {

    private final IntentClassificationService intentClassificationService;
    private final HybridSearchService hybridSearchService;
    private final AnswerGenerationService answerGenerationService;
    private final SuggestedQuestionService suggestedQuestionService;

    public ChatResponse handle(ChatRequest request) {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();

        var route = intentClassificationService.route(request.message());

        if ("feature".equals(route.category())) {
            if (route.featureType() == null) {
                return new ChatResponse(
                        sessionId,
                        "어떤 기능을 원하시는지 정확히 파악하지 못했어요. 조금 더 구체적으로 말씀해주시겠어요?",
                        route.category(), List.of(), List.of(), null
                );
            }
            FeatureType feature = route.featureType();
            String answer = feature.description() + "이에요. " + feature.label() + "으로 이동하시겠어요?";
            RedirectDto redirect = new RedirectDto(feature.name(), feature.label(), feature.path());
            return new ChatResponse(
                    sessionId,
                    answer,
                    route.category(),
                    List.of(),
                    List.of(),
                    redirect
            );
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

        return new ChatResponse(sessionId, answer, route.category(), sources, suggestedQuestions, null);
    }
}
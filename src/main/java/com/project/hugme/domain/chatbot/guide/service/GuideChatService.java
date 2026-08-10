package com.project.hugme.domain.chatbot.guide.service;

import com.project.hugme.domain.chatbot.guide.dto.ChatRequest;
import com.project.hugme.domain.chatbot.guide.dto.ChatResponse;
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

    public ChatResponse handle(ChatRequest request) {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();

        var route = intentClassificationService.route(request.message());

        if ("feature".equals(route.category())) {
            return new ChatResponse(
                    sessionId,
                    "해당 기능으로 안내해드릴까요?",
                    route.category(),
                    List.of(),
                    List.of(),
                    null // TODO: feature 매핑 로직 추가
            );
        }

        List<Document> results = hybridSearchService.search(request.message(), route.sources());
        String answer = answerGenerationService.generate(request.message(), results);

        List<SourceDto> sources = results.stream()
                .map(doc -> new SourceDto(
                        String.valueOf(doc.getMetadata().getOrDefault("source", "unknown")),
                        doc.getText().length() > 100 ? doc.getText().substring(0, 100) + "..." : doc.getText()
                ))
                .toList();

        return new ChatResponse(sessionId, answer, route.category(), sources, List.of(), null);
    }
}
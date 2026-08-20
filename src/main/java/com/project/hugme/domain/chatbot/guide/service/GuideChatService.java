package com.project.hugme.domain.chatbot.guide.service;

import com.project.hugme.domain.chatbot.FeatureType;
import com.project.hugme.domain.chatbot.guide.dto.ChatRequest;
import com.project.hugme.domain.chatbot.guide.dto.ChatResponse;
import com.project.hugme.domain.chatbot.guide.dto.EntryQuestion;
import com.project.hugme.domain.chatbot.guide.dto.RedirectDto;
import com.project.hugme.domain.chatbot.guide.dto.SourceDto;
import com.project.hugme.domain.chatbot.guide.entity.GuideChatHistory;
import com.project.hugme.domain.chatbot.guide.repository.GuideChatHistoryRepository;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    private final ChatMemory chatMemory;
    private final GuideSessionService guideSessionService;

    public Flux<ServerSentEvent<Object>> handle(Long userId, ChatRequest request) {
        String sessionId = resolveSessionId(request);
        guideSessionService.rehydrateMemoryIfNeeded(userId, sessionId);

        var route = intentClassificationService.route(request.message(), sessionId);

        if ("feature".equals(route.category())) {
            if (route.featureType() == null) {
                ChatResponse response = new ChatResponse(
                        sessionId,
                        "어떤 기능을 원하시는지 정확히 파악하지 못했어요. 조금 더 구체적으로 말씀해주시겠어요?",
                        route.category(), List.of(), List.of(), null
                );
                saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
                rememberTurn(sessionId, request.message(), response.answer());
                return streamFinalAnswer(response);
            }
            FeatureType feature = route.featureType();
            String answer = "해당 요청은 " + feature.label() + " 기능이에요. " + feature.label() + "으로 이동하시겠어요?";
            RedirectDto redirect = new RedirectDto(feature.name(), feature.label(), feature.path());
            ChatResponse response = new ChatResponse(
                    sessionId, answer, route.category(), List.of(), List.of(), redirect
            );
            saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
            rememberTurn(sessionId, request.message(), response.answer());
            return streamFinalAnswer(response);
        }

        if ("suggestion_request".equals(route.category())) {
            List<String> suggestions = suggestedQuestionService.suggestFromHistory(chatMemory.get(sessionId));
            boolean hasContext = !suggestions.isEmpty();
            List<String> finalSuggestions = hasContext
                    ? suggestions
                    : EntryQuestion.defaults().stream()
                            .flatMap(entry -> entry.questions().stream())
                            .limit(4)
                            .toList();
            String answer = hasContext
                    ? "이런 질문은 어떠세요?"
                    : "아직 나눈 대화가 없어서, 자주 묻는 질문을 추천해드릴게요.";
            ChatResponse response = new ChatResponse(
                    sessionId, answer, route.category(), List.of(), finalSuggestions, null
            );
            saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
            rememberTurn(sessionId, request.message(), response.answer());
            return streamFinalAnswer(response);
        }

        if ("meta".equals(route.category())) {
            String answer = metaAnswerService.generate(sessionId, request.message());
            ChatResponse response = new ChatResponse(sessionId, answer, route.category(), List.of(), List.of(), null);
            saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
            return streamFinalAnswer(response);
        }

        if ("off_topic".equals(route.category())) {
            ChatResponse response = new ChatResponse(
                    sessionId,
                    "죄송해요, 저는 상담과 관련된 내용만 도와드릴 수 있어요.",
                    route.category(), List.of(), List.of(), null
            );
            saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
            rememberTurn(sessionId, request.message(), response.answer());
            return streamFinalAnswer(response);
        }

        List<Document> results = hybridSearchService.search(route.rewrittenQuery(), route.category());

        List<SourceDto> sources = results.stream()
                .map(doc -> new SourceDto(
                        String.valueOf(doc.getMetadata().getOrDefault("source", "unknown")),
                        doc.getText().length() > 100 ? doc.getText().substring(0, 100) + "..." : doc.getText()
                ))
                .toList();

        StringBuilder answerBuilder = new StringBuilder();

        Flux<ServerSentEvent<Object>> tokenEvents = answerGenerationService
                .generateStream(sessionId, request.message(), results)
                .doOnNext(answerBuilder::append)
                .map(chunk -> ServerSentEvent.builder((Object) chunk).event("token").build());

        Mono<ServerSentEvent<Object>> doneEvent = Mono.fromCallable(() -> {
                    String answer = answerBuilder.toString();
                    List<String> suggestedQuestions = suggestedQuestionService.suggest(request.message(), answer, results);
                    ChatResponse response = new ChatResponse(
                            sessionId, answer, route.category(), sources, suggestedQuestions, null
                    );
                    saveHistoryIfLoggedIn(userId, sessionId, request.message(), response);
                    return ServerSentEvent.builder((Object) response).event("done").build();
                })
                .subscribeOn(Schedulers.boundedElastic());

        return tokenEvents.concatWith(doneEvent);
    }

    private Flux<ServerSentEvent<Object>> streamFinalAnswer(ChatResponse response) {
        return Flux.just(
                ServerSentEvent.builder((Object) response.answer()).event("token").build(),
                ServerSentEvent.builder((Object) response).event("done").build()
        );
    }

    // feature/off_topic 답변은 chatClient를 거치지 않아 MessageChatMemoryAdvisor가 자동으로 기록해주지 않으므로 직접 기록한다.
    // meta는 metaAnswerService가 chatClient를 통해 이미 기록하므로 여기서 다시 호출하지 않는다.
    private void rememberTurn(String sessionId, String question, String answer) {
        chatMemory.add(sessionId, List.of(new UserMessage(question), new AssistantMessage(answer)));
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
        guideSessionService.pruneOldSessions(userId);
    }

    private String resolveSessionId(ChatRequest request) {
        return request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();
    }
}
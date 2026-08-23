package com.project.hugme.domain.chatbot.document.service;

import com.project.hugme.domain.chatbot.document.dto.DocumentChatHistoryResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentChatSessionResponse;
import com.project.hugme.domain.chatbot.document.entity.DocumentChatHistory;
import com.project.hugme.domain.chatbot.document.repository.DocumentChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentChatHistoryService {

    private static final int MAX_SESSIONS_RETURNED = 20;
    private final DocumentChatHistoryRepository historyRepository;

    public List<DocumentChatSessionResponse> listSessions(Long userId) {
        Map<String, List<DocumentChatHistory>> sessions = new LinkedHashMap<>();
        for (DocumentChatHistory history : historyRepository.findByUserUserIdOrderByCreatedAtAsc(userId)) {
            sessions.computeIfAbsent(history.getSessionId(), ignored -> new ArrayList<>()).add(history);
        }

        return sessions.values().stream()
                .map(messages -> {
                    DocumentChatHistory first = messages.get(0);
                    DocumentChatHistory last = messages.get(messages.size() - 1);
                    return new DocumentChatSessionResponse(
                            first.getSessionId(),
                            first.getApplicationId(),
                            first.getQuestion(),
                            first.getCreatedAt(),
                            last.getCreatedAt());
                })
                .sorted(Comparator.comparing(DocumentChatSessionResponse::lastMessageAt).reversed())
                .limit(MAX_SESSIONS_RETURNED)
                .toList();
    }

    public List<DocumentChatHistoryResponse> getSessionHistory(Long userId, String sessionId) {
        return historyRepository.findByUserUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId)
                .stream()
                .map(history -> new DocumentChatHistoryResponse(
                        history.getHistoryId(),
                        history.getApplicationId(),
                        history.getDocumentId(),
                        history.getQuestion(),
                        history.getAnswer(),
                        splitSources(history.getSources()),
                        history.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        historyRepository.deleteByUserUserIdAndSessionId(userId, sessionId);
    }

    private List<String> splitSources(String sources) {
        if (sources == null || sources.isBlank()) {
            return List.of();
        }
        return sources.lines().filter(source -> !source.isBlank()).toList();
    }
}

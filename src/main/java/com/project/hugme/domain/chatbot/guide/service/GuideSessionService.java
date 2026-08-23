package com.project.hugme.domain.chatbot.guide.service;

import com.project.hugme.domain.chatbot.guide.dto.GuideChatHistoryDto;
import com.project.hugme.domain.chatbot.guide.dto.SessionSummaryDto;
import com.project.hugme.domain.chatbot.guide.entity.GuideChatHistory;
import com.project.hugme.domain.chatbot.guide.repository.GuideChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GuideSessionService {

    private static final int MAX_SESSIONS_RETURNED = 20;

    private final GuideChatHistoryRepository guideChatHistoryRepository;
    private final ChatMemory chatMemory;

    public List<SessionSummaryDto> listSessions(Long userId) {
        List<GuideChatHistory> history = guideChatHistoryRepository.findByUser_UserIdOrderByCreatedAtAsc(userId);

        Map<String, List<GuideChatHistory>> bySession = new LinkedHashMap<>();
        for (GuideChatHistory entry : history) {
            bySession.computeIfAbsent(entry.getSessionId(), key -> new ArrayList<>()).add(entry);
        }

        return bySession.values().stream()
                .map(turns -> {
                    GuideChatHistory first = turns.get(0);
                    GuideChatHistory last = turns.get(turns.size() - 1);
                    return new SessionSummaryDto(
                            first.getSessionId(), first.getQuestion(), first.getCreatedAt(), last.getCreatedAt()
                    );
                })
                .sorted(Comparator.comparing(SessionSummaryDto::lastMessageAt).reversed())
                .limit(MAX_SESSIONS_RETURNED)
                .toList();
    }

    public List<GuideChatHistoryDto> getSessionHistory(Long userId, String sessionId) {
        return guideChatHistoryRepository.findByUser_UserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId).stream()
                .map(h -> new GuideChatHistoryDto(
                        h.getHistoryId(), h.getCategory(), h.getQuestion(), h.getAnswer(), h.getSources(), h.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        guideChatHistoryRepository.deleteByUser_UserIdAndSessionId(userId, sessionId);
        chatMemory.clear(sessionId);
    }

    public void rehydrateMemoryIfNeeded(Long userId, String sessionId) {
        if (userId == null || !chatMemory.get(sessionId).isEmpty()) {
            return;
        }

        List<GuideChatHistory> history = guideChatHistoryRepository
                .findByUser_UserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);

        if (history.isEmpty()) {
            return;
        }

        List<Message> messages = history.stream()
                .skip(Math.max(0, history.size() - 10))
                .flatMap(h -> Stream.<Message>of(
                        new UserMessage(h.getQuestion()),
                        new AssistantMessage(h.getAnswer())
                ))
                .toList();

        chatMemory.add(sessionId, messages);
    }
}

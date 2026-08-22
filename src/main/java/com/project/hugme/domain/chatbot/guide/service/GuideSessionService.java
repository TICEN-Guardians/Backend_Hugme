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

    private static final int MAX_SESSIONS_PER_USER = 10;

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

    /**
     * 세션이 MAX_SESSIONS_PER_USER개를 넘으면 가장 오래된(최근 메시지 기준) 세션부터 정리한다.
     * 새 세션의 첫 메시지가 저장된 직후에만 호출해도 충분하다.
     */
    @Transactional
    public void pruneOldSessions(Long userId) {
        List<SessionSummaryDto> sessions = listSessions(userId);
        if (sessions.size() <= MAX_SESSIONS_PER_USER) {
            return;
        }

        sessions.stream()
                .skip(MAX_SESSIONS_PER_USER)
                .forEach(session -> deleteSession(userId, session.sessionId()));
    }

    /**
     * 인메모리 ChatMemory가 비어있는데 DB에는 해당 세션 이력이 있으면(서버 재시작, 다른 세션에서 재진입 등)
     * DB 이력을 다시 채워 넣어 LLM이 맥락을 이어갈 수 있게 한다.
     */
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

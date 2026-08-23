package com.project.hugme.domain.chatbot.document.repository;

import com.project.hugme.domain.chatbot.document.entity.DocumentChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChatHistoryRepository extends JpaRepository<DocumentChatHistory, Long> {
    List<DocumentChatHistory> findByUserUserIdOrderByCreatedAtAsc(Long userId);

    List<DocumentChatHistory> findByUserUserIdAndSessionIdOrderByCreatedAtAsc(
            Long userId, String sessionId);

    void deleteByUserUserIdAndSessionId(Long userId, String sessionId);
}

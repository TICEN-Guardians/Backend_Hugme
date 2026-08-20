package com.project.hugme.domain.chatbot.guide.repository;

import com.project.hugme.domain.chatbot.guide.entity.GuideChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuideChatHistoryRepository extends JpaRepository<GuideChatHistory, Long> {

    List<GuideChatHistory> findByUser_UserIdOrderByCreatedAtAsc(Long userId);

    List<GuideChatHistory> findByUser_UserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);

    void deleteByUser_UserIdAndSessionId(Long userId, String sessionId);

}
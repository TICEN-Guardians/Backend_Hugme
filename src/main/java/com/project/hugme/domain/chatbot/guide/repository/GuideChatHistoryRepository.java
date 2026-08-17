package com.project.hugme.domain.chatbot.guide.repository;

import com.project.hugme.domain.chatbot.guide.entity.GuideChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuideChatHistoryRepository extends JpaRepository<GuideChatHistory, Long> {
}
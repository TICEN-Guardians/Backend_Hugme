package com.project.hugme.domain.chatbot.document.repository;

import com.project.hugme.domain.chatbot.document.entity.DocumentChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentChatHistoryRepository extends JpaRepository<DocumentChatHistory, Long> {
}

package com.project.hugme.domain.chatbot.document.dto;

import java.time.Instant;

public record DocumentChatSessionResponse(
        String sessionId,
        Long applicationId,
        String title,
        Instant createdAt,
        Instant lastMessageAt
) {
}

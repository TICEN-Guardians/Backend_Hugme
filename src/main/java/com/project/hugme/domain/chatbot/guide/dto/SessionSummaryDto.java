package com.project.hugme.domain.chatbot.guide.dto;

import java.time.Instant;

public record SessionSummaryDto(
        String sessionId,
        String title,
        Instant createdAt,
        Instant lastMessageAt
) {}

package com.project.hugme.domain.chatbot.guide.dto;

import java.time.Instant;

public record GuideChatHistoryDto(
        Long historyId,
        String category,
        String question,
        String answer,
        String sources,
        Instant createdAt
) {}
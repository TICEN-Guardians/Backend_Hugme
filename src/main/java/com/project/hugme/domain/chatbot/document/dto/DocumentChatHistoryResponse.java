package com.project.hugme.domain.chatbot.document.dto;

import java.time.Instant;
import java.util.List;

public record DocumentChatHistoryResponse(
        Long historyId,
        Long applicationId,
        Long documentId,
        String question,
        String answer,
        List<String> sources,
        Instant createdAt
) {
}

package com.project.hugme.domain.chatbot.document.dto;

import java.util.List;

public record DocumentChatResponse(
        String answer,
        List<DocumentSourceResponse> sources
) {
}

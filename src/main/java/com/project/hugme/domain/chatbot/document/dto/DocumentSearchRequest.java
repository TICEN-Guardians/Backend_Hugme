package com.project.hugme.domain.chatbot.document.dto;

public record DocumentSearchRequest(
        Long applicationId,
        Long documentId,
        String question,
        String sessionId
) {
    public DocumentSearchRequest(Long documentId, String question) {
        this(null, documentId, question, null);
    }
}

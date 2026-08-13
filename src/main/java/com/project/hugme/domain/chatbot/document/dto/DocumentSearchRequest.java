package com.project.hugme.domain.chatbot.document.dto;

public record DocumentSearchRequest(
        Long documentId,
        String question
) {
}
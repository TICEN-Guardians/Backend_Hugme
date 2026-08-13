package com.project.hugme.infra.ai.document.dto;

public record DocumentSearchRequest(
        Long documentId,
        String question
) {
}
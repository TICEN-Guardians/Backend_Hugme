package com.project.hugme.infra.elasticsearch.document.dto;

public record DocumentSearchRequest(
        Long documentId,
        String question
) {
}
package com.project.hugme.domain.checklist.dto.product;

public record DocumentResponse(
        Long documentId,
        String documentName,
        String description,
        String sampleImageUrl,
        Integer sortOrder,
        DocumentGroupResponse documentGroup


) {
}

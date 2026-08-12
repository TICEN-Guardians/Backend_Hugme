package com.project.hugme.domain.checklist.dto.application;

import com.project.hugme.domain.checklist.dto.product.DocumentGroupResponse;

public record MyDocumentResponse(
        Long documentId,
        String documentName,
        String description,
        String sampleImageUrl,
        Integer sortOrder,
        DocumentGroupResponse documentGroup
) {
}
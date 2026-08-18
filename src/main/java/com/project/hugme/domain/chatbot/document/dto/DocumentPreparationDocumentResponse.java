package com.project.hugme.domain.chatbot.document.dto;

import com.project.hugme.domain.checklist.entity.product.Document;
import com.project.hugme.domain.checklist.entity.product.DocumentGroup;

public record DocumentPreparationDocumentResponse(
        Long documentId,
        String documentName,
        String description,
        Integer sortOrder,
        Long documentGroupId,
        String documentGroupName,
        Integer documentGroupSortOrder,
        boolean prepared
) {
    public static DocumentPreparationDocumentResponse from(Document document, boolean prepared) {
        DocumentGroup group = document.getDocumentGroup();
        return new DocumentPreparationDocumentResponse(
                document.getDocumentId(), document.getDocumentName(), document.getDescription(),
                document.getSortOrder(),
                group == null ? null : group.getDocumentGroupId(),
                group == null ? null : group.getGroupName(),
                group == null ? null : group.getSortOrder(), prepared
        );
    }
}

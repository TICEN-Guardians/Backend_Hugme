package com.project.hugme.domain.checklist.dto.common;

import com.project.hugme.domain.checklist.entity.product.Document;
import com.project.hugme.domain.checklist.entity.product.DocumentGroup;

public record DocumentResponse(
        Long documentId,
        String documentName,
        String description,
        String sampleImageUrl,
        Integer sortOrder,

        Long documentGroupId,
        String documentGroupName,
        Integer documentGroupSortOrder


) {

    public static DocumentResponse from(
            Document document
    ) {
        DocumentGroup documentGroup =
                document.getDocumentGroup();

        return new DocumentResponse(
                document.getDocumentId(),
                document.getDocumentName(),
                document.getDescription(),
                document.getSampleImageUrl(),
                document.getSortOrder(),

                documentGroup == null
                        ? null
                        : documentGroup.getDocumentGroupId(),

                documentGroup == null
                        ? null
                        : documentGroup.getGroupName(),

                documentGroup == null
                        ? null
                        : documentGroup.getSortOrder()
        );
    }
}

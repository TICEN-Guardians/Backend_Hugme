package com.project.hugme.domain.checklist.dto.application;

import com.project.hugme.domain.checklist.dto.common.DocumentResponse;

import java.util.List;

public record MyDocumentItemResponse(

        Long itemId,
        String itemName,
        Integer sortOrder,
        Boolean defaultIncluded,

        Long groupId,
        String groupName,
        Integer groupSortOrder,

        List<DocumentResponse> documents
) {
}

package com.project.hugme.domain.checklist.dto.product;

import com.project.hugme.domain.checklist.dto.common.DocumentResponse;
import com.project.hugme.domain.checklist.entity.product.SectionCode;

import java.util.List;

public record ChecklistItemResponse(
        Long itemId,
        String itemName,
        Integer sortOrder,
        Boolean defaultIncluded,

        SectionCode sectionCode,
        String sectionName,

        Long groupId,
        String groupName,
        Integer groupSortOrder,

        List<DocumentResponse> documents
) {
}


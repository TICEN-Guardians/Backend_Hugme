package com.project.hugme.domain.checklist.dto.product;

import java.util.List;

public record ChecklistItemResponse(
        Long itemId,
        String itemName,
        Integer sortOrder,
        Boolean defaultIncluded,
        ChecklistGroupResponse checklistGroup,
        List<DocumentResponse> documents

) {
}
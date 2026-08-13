package com.project.hugme.domain.checklist.dto.product;

public record ChecklistItemResponse(
        Long itemId,
        String itemName,
        Integer sortOrder,
        Boolean defaultIncluded,

        Long groupId,
        String groupName,
        Integer groupSortOrder

) {
}


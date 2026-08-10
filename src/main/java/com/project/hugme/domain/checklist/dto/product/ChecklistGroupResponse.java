package com.project.hugme.domain.checklist.dto.product;

public record ChecklistGroupResponse(
        Long groupId,
        String groupName,
        Integer sortOrder
) {
}

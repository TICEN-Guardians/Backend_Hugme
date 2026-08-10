package com.project.hugme.domain.checklist.dto;

public record ChecklistGroupResponse(
        Long groupId,
        String groupName,
        Integer sortOrder
) {
}

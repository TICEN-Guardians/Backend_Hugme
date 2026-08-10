package com.project.hugme.domain.checklist.dto;

public record DocumentGroupResponse(
        Long documentGroupId,
        String groupName,
        Integer sortOrder

) {
}

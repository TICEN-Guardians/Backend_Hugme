package com.project.hugme.domain.checklist.dto;

import com.project.hugme.domain.checklist.entity.SectionCode;

import java.util.List;

public record ChecklistSectionResponse(
        Long sectionId,
        SectionCode sectionCode,
        String sectionName,
        List<ChecklistItemResponse> items

) {
}

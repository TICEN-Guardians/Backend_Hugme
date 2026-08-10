package com.project.hugme.domain.checklist.dto.product;

import com.project.hugme.domain.checklist.entity.product.SectionCode;

import java.util.List;

public record ChecklistSectionResponse(
        Long sectionId,
        SectionCode sectionCode,
        String sectionName,
        List<ChecklistItemResponse> items

) {
}

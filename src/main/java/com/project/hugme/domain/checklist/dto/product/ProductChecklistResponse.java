package com.project.hugme.domain.checklist.dto.product;

import com.project.hugme.domain.checklist.entity.product.ProductCode;
import com.project.hugme.domain.checklist.entity.product.SectionCode;

import java.util.List;

public record ProductChecklistResponse(
        ProductCode productCode,
        String productName,
        SectionCode sectionCode,
        String sectionName,
        List<ChecklistItemResponse> items
) {
}
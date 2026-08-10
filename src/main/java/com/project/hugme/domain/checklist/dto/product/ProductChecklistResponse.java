package com.project.hugme.domain.checklist.dto.product;

import com.project.hugme.domain.checklist.dto.ChecklistSectionResponse;
import com.project.hugme.domain.checklist.entity.ProductCode;

import java.util.List;

public record ProductChecklistResponse(
        ProductCode productCode,
        String productName,
        List<ChecklistSectionResponse> sections) {


}

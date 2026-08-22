package com.project.hugme.domain.checklist.dto.application;

import com.project.hugme.domain.checklist.entity.product.SectionCode;

import java.util.List;

public record ResultDocumentSectionResponse(
        SectionCode sectionCode,
        String sectionName,
        List<ResultDocumentItemResponse> items

) {
}

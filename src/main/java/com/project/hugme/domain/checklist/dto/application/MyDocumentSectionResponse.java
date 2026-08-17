package com.project.hugme.domain.checklist.dto.application;

import com.project.hugme.domain.checklist.dto.common.DocumentResponse;
import com.project.hugme.domain.checklist.entity.product.SectionCode;

import java.util.List;

public record MyDocumentSectionResponse(
        SectionCode sectionCode,
        String sectionName,
        List<MyDocumentListResponse> items
) {
}
package com.project.hugme.domain.chatbot.document.dto;

import com.project.hugme.domain.checklist.entity.product.SectionCode;
import java.util.List;

public record DocumentPreparationSectionResponse(
        SectionCode sectionCode,
        String sectionName,
        List<DocumentPreparationDocumentResponse> documents
) {}

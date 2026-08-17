package com.project.hugme.domain.chatbot.document.dto;

import java.util.List;

public record DocumentPreparationResponse(
        Long applicationId,
        int totalDocumentCount,
        int preparedDocumentCount,
        List<DocumentPreparationSectionResponse> sections
) {}

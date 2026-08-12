package com.project.hugme.domain.checklist.dto.application;

import java.util.List;

public record MyDocumentListResponse(
        Long applicationId,
        List<MyDocumentSectionResponse> sections
) {
}
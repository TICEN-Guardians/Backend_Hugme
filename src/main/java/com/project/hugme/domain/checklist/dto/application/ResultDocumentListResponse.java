package com.project.hugme.domain.checklist.dto.application;

import java.util.List;

public record ResultDocumentListResponse(
        Long applicationId,
        List<ResultDocumentSectionResponse> sections

) {
}

package com.project.hugme.infra.elasticsearch.document.dto;

import java.util.List;
import java.util.Map;

public record DocumentSearchResponse(
        Long documentId,
        String documentName,
        Map<String, Object> fields,
        String officialGuideUrl,
        List<String> hugReferenceUrls,
        Double score
) {
}
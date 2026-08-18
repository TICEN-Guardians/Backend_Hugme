package com.project.hugme.domain.diagnosis.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RegistryOcrResponse(
        String parseStatus,
        String parseConfidence,
        String propertyAddress,
        String dongName,
        Integer floor,
        String hoName,
        BigDecimal exclusiveArea,
        List<Owner> currentOwners,
        String overallMatchStatus
) {
    public record Owner(String name, String share) {}
}

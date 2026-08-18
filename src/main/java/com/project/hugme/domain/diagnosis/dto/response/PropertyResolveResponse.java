package com.project.hugme.domain.diagnosis.dto.response;

import com.project.hugme.domain.diagnosis.enums.HousingType;
import java.math.BigDecimal;

public record PropertyResolveResponse(
        String normalizedAddress,
        String buildingName,
        String dongName,
        String hoName,
        HousingType housingType,
        boolean contractAreaRequired,
        boolean unitNumberRequired,
        BigDecimal exclusiveArea,
        BigDecimal commonArea,
        BigDecimal totalArea
) {
}
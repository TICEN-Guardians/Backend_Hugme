package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.dto.response.PropertyResolveResponse;
import com.project.hugme.domain.diagnosis.enums.HousingType;
import java.math.BigDecimal;

public record FastApiPropertyResolveResponse(
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

    public PropertyResolveResponse toResponse() {
        return new PropertyResolveResponse(
                normalizedAddress,
                buildingName,
                dongName,
                hoName,
                housingType,
                contractAreaRequired,
                unitNumberRequired,
                exclusiveArea,
                commonArea,
                totalArea
        );
    }
}
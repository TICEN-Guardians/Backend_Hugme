package com.project.hugme.domain.diagnosis.dto.response;

import com.project.hugme.domain.diagnosis.enums.HousingType;

public record PropertyResolveResponse(
        String normalizedAddress,
        String buildingName,
        String dongName,
        HousingType housingType,
        boolean contractAreaRequired
) {
}
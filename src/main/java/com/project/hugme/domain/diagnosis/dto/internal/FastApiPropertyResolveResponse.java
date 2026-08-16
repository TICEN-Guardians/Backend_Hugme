package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.dto.response.PropertyResolveResponse;
import com.project.hugme.domain.diagnosis.enums.HousingType;

public record FastApiPropertyResolveResponse(
        String normalizedAddress,
        String buildingName,
        String dongName,
        HousingType housingType,
        boolean contractAreaRequired
) {

    public PropertyResolveResponse toResponse() {
        return new PropertyResolveResponse(
                normalizedAddress,
                buildingName,
                dongName,
                housingType,
                contractAreaRequired
        );
    }
}
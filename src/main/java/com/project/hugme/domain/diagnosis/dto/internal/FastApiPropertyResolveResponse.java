package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.dto.response.PropertyResolveResponse;
import com.project.hugme.domain.diagnosis.enums.HousingType;

public record FastApiPropertyResolveResponse(
        String normalizedAddress,
        HousingType housingType,
        boolean contractAreaRequired
) {

    public PropertyResolveResponse toResponse() {
        return new PropertyResolveResponse(
                normalizedAddress,
                housingType,
                contractAreaRequired
        );
    }
}
package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.dto.response.PropertySearchResponse;
import com.project.hugme.domain.diagnosis.dto.response.PropertySearchResponse.PropertyCandidateResponse;
import com.project.hugme.domain.diagnosis.enums.HousingType;

import java.util.List;

public record FastApiPropertySearchResponse(
        String normalizedAddress,
        String buildingName,
        List<FastApiPropertyCandidateResponse> candidates
) {

    public PropertySearchResponse toResponse() {
        List<PropertyCandidateResponse> responses =
                candidates == null
                        ? List.of()
                        : candidates.stream()
                          .map(FastApiPropertyCandidateResponse::toResponse)
                          .toList();

        return new PropertySearchResponse(
                normalizedAddress,
                buildingName,
                responses
        );
    }

    public record FastApiPropertyCandidateResponse(
            String buildingName,
            String dongName,
            HousingType housingType
    ) {

        private PropertyCandidateResponse toResponse() {
            return new PropertyCandidateResponse(
                    buildingName,
                    dongName,
                    housingType
            );
        }
    }
}
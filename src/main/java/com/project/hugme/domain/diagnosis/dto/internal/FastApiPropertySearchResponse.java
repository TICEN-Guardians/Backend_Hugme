package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.dto.response.PropertySearchResponse;
import com.project.hugme.domain.diagnosis.dto.response.PropertySearchResponse.AddressCandidateResponse;
import com.project.hugme.domain.diagnosis.dto.response.PropertySearchResponse.PropertyCandidateResponse;
import com.project.hugme.domain.diagnosis.enums.HousingType;

import java.util.List;

public record FastApiPropertySearchResponse(
        String normalizedAddress,
        String roadAddress,
        String jibunAddress,
        String buildingName,
        List<FastApiPropertyCandidateResponse> candidates,
        List<FastApiAddressCandidateResponse> addressCandidates
) {

    public PropertySearchResponse toResponse() {
        List<PropertyCandidateResponse> responses =
                candidates == null
                        ? List.of()
                        : candidates.stream()
                          .map(FastApiPropertyCandidateResponse::toResponse)
                          .toList();

        List<AddressCandidateResponse> addresses =
                addressCandidates == null
                        ? List.of()
                        : addressCandidates.stream()
                          .map(FastApiAddressCandidateResponse::toResponse)
                          .toList();

        return new PropertySearchResponse(
                normalizedAddress,
                roadAddress,
                jibunAddress,
                buildingName,
                responses,
                addresses
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

    public record FastApiAddressCandidateResponse(
            String roadAddress,
            String jibunAddress,
            String buildingName
    ) {

        private AddressCandidateResponse toResponse() {
            return new AddressCandidateResponse(
                    roadAddress,
                    jibunAddress,
                    buildingName
            );
        }
    }
}

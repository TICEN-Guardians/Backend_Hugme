package com.project.hugme.domain.diagnosis.dto.response;
import com.project.hugme.domain.diagnosis.enums.HousingType;

import java.util.List;

public record PropertySearchResponse(
        String normalizedAddress,
        String roadAddress,
        String jibunAddress,
        String buildingName,
        List<PropertyCandidateResponse> candidates,
        List<AddressCandidateResponse> addressCandidates
) {

    public record PropertyCandidateResponse(
            String buildingName,
            String dongName,
            HousingType housingType
    ) {
    }

    public record AddressCandidateResponse(
            String roadAddress,
            String jibunAddress,
            String buildingName
    ) {
    }
}

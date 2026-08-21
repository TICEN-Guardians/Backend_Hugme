package com.project.hugme.domain.diagnosis.dto.response;

import java.util.List;

public record AddressSuggestionResponse(
        List<Candidate> candidates
) {
    public record Candidate(
            String roadAddress,
            String jibunAddress,
            String buildingName
    ) {
    }
}

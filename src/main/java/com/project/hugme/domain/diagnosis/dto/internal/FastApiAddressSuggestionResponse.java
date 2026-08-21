package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.dto.response.AddressSuggestionResponse;

import java.util.List;

public record FastApiAddressSuggestionResponse(
        List<Candidate> candidates
) {
    public AddressSuggestionResponse toResponse() {
        List<AddressSuggestionResponse.Candidate> items = candidates == null
                ? List.of()
                : candidates.stream()
                        .map(Candidate::toResponse)
                        .toList();
        return new AddressSuggestionResponse(items);
    }

    public record Candidate(
            String roadAddress,
            String jibunAddress,
            String buildingName
    ) {
        private AddressSuggestionResponse.Candidate toResponse() {
            return new AddressSuggestionResponse.Candidate(
                    roadAddress,
                    jibunAddress,
                    buildingName
            );
        }
    }
}

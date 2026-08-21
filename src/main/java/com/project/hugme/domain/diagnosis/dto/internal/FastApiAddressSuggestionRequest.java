package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.dto.request.AddressSuggestionRequest;

public record FastApiAddressSuggestionRequest(
        String address
) {
    public static FastApiAddressSuggestionRequest from(
            AddressSuggestionRequest request
    ) {
        return new FastApiAddressSuggestionRequest(request.address());
    }
}

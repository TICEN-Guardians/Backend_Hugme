package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.dto.request.PropertySearchRequest;

public record FastApiPropertySearchRequest(
        String address
) {

    public static FastApiPropertySearchRequest from(
            PropertySearchRequest request
    ) {
        return new FastApiPropertySearchRequest(
                request.address()
        );
    }
}
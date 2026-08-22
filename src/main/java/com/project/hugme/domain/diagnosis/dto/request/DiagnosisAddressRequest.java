package com.project.hugme.domain.diagnosis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record DiagnosisAddressRequest(
        @NotBlank @Size(max = 500) String address,
        @Size(min = 1, max = 100) String dongName,
        @Size(max = 100) String hoName,
        Map<String, Object> propertySnapshot,
        boolean registryAddressReviewConfirmed
) {
}

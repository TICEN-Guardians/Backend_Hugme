package com.project.hugme.domain.diagnosis.dto.request;

import com.project.hugme.domain.diagnosis.enums.DiagnosisMode;
import jakarta.validation.constraints.NotNull;

public record DiagnosisCreateRequest(
        @NotNull DiagnosisMode mode
) {
}

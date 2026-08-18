package com.project.hugme.domain.diagnosis.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DiagnosisDetailsRequest(
        @Size(min = 1, max = 100) String dongName,
        @Size(max = 100) String hoName,
        @NotNull @Positive Long deposit,
        @NotNull LocalDate contractDate,
        @DecimalMin("0.01") BigDecimal contractArea,
        @DecimalMin("0.01") BigDecimal exclusiveArea,
        @NotNull Integer floor,
        @Size(max = 100) String landlordName
) {}

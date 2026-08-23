package com.project.hugme.domain.diagnosis.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record DiagnosisWhatIfRequest(
        @NotNull
        @Positive
        Long deposit,

        @NotNull
        @Min(0)
        @Max(50)
        Integer salePriceDropRate,

        @NotNull
        @Min(0)
        @Max(50)
        Integer leasePriceDropRate,

        @PositiveOrZero
        Long activeMaxClaimAmount,

        boolean removeActiveMortgage
) {
}

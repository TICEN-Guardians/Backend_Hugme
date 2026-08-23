package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.dto.request.DiagnosisWhatIfRequest;
import com.project.hugme.domain.diagnosis.entity.Diagnosis;

import java.util.List;

public record FastApiDiagnosisWhatIfRequest(
        String mode,
        Long estimatedSalePrice,
        Long estimatedLeasePrice,
        Long baselineDeposit,
        Long scenarioDeposit,
        Integer salePriceDropRate,
        Integer leasePriceDropRate,
        Long activeMaxClaimAmount,
        Long scenarioActiveMaxClaimAmount,
        boolean removeActiveMortgage,
        Integer marketTrendScore,
        List<String> unresolvedRiskReasons
) {
    public static FastApiDiagnosisWhatIfRequest from(
            Diagnosis diagnosis,
            FastApiDiagnosisResponse response,
            DiagnosisWhatIfRequest request
    ) {
        Long activeMaxClaimAmount = activeMaxClaimAmount(
                diagnosis,
                response
        );
        return new FastApiDiagnosisWhatIfRequest(
                diagnosis.getMode().name(),
                response.valuation().estimatedSalePrice(),
                response.valuation().estimatedLeasePrice(),
                diagnosis.getDeposit(),
                request.deposit(),
                request.salePriceDropRate(),
                request.leasePriceDropRate(),
                activeMaxClaimAmount,
                request.activeMaxClaimAmount(),
                request.removeActiveMortgage(),
                response.risk().breakdown().marketTrend(),
                response.forcedWarnings() == null
                        ? List.of()
                        : List.copyOf(response.forcedWarnings())
        );
    }

    private static Long activeMaxClaimAmount(
            Diagnosis diagnosis,
            FastApiDiagnosisResponse response
    ) {
        Long burden = response.indicators().collateralBurdenAmount();
        if (burden == null) {
            return null;
        }
        long activeMaxClaimAmount = burden - diagnosis.getDeposit();
        if (activeMaxClaimAmount < 0) {
            throw new IllegalStateException(
                    "저장된 담보부담액이 계약 보증금보다 작습니다."
            );
        }
        return activeMaxClaimAmount;
    }
}

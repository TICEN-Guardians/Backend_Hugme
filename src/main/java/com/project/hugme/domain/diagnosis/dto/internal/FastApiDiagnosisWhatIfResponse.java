package com.project.hugme.domain.diagnosis.dto.internal;

import java.util.List;

public record FastApiDiagnosisWhatIfResponse(
        Scenario baseline,
        Scenario scenario,
        Integer scoreChange,
        Boolean gradeChanged,
        Boolean registryBlockersRemain,
        List<String> unresolvedRiskReasons,
        FastApiDiagnosisResponse.DepositRecommendation depositRecommendation
) {
    public record Scenario(
            FastApiDiagnosisResponse.Valuation valuation,
            Long deposit,
            Long activeMaxClaimAmount,
            FastApiDiagnosisResponse.Indicators indicators,
            FastApiDiagnosisResponse.Risk risk
    ) {
    }
}

package com.project.hugme.domain.diagnosis.dto.internal;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record FastApiDiagnosisResponse(
        Long analysisId,
        String status,
        Instant analyzedAt,
        Property property,
        Valuation valuation,
        Indicators indicators,
        Risk risk,
        List<String> forcedWarnings,
        List<String> missingChecks,
        String valuationReliability,
        List<String> dataWarnings,
        List<String> fallbackFeatures,
        String report
) {
    public record Property(
            String normalizedAddress,
            String housingType
    ) {
    }

    public record Valuation(
            Long estimatedSalePrice,
            Long estimatedLeasePrice
    ) {
    }

    public record Indicators(
            Double leaseToSaleRate,
            Double leasePriceGapRate,
            Long collateralBurdenAmount,
            Double collateralBurdenRate,
            Long recoverableAmount,
            Long depositShortfall,
            Long remainingCollateralCapacity,
            Map<String, Double> priceDropScenarios
    ) {
    }

    public record Risk(
            Integer score,
            String grade,
            Breakdown breakdown
    ) {
    }

    public record Breakdown(
            Integer underwater,
            Integer rollover,
            Integer property,
            Integer market
    ) {
    }
}

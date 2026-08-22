package com.project.hugme.domain.diagnosis.dto.internal;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record FastApiDiagnosisResponse(
        Long analysisId,
        String mode,
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
        String report,
        ReportDetail reportDetail
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
            Integer baseScore,
            String grade,
            Breakdown breakdown,
            Weights weights,
            Integer scoreFloor,
            List<String> floorReasons,
            Boolean scoreFloorApplied,
            Boolean provisionalCollateralBasis
    ) {
    }

    public record Breakdown(
            Integer priceBurden,
            Integer leaseMarketDeviation,
            Integer marketTrend,
            Integer policyAdjustment,
            Integer rightsAdjustment
    ) {
    }

    public record Weights(
            Integer priceBurden,
            Integer leaseMarketDeviation,
            Integer marketTrend,
            Integer total
    ) {
    }

    public record ReportDetail(
            String title,
            String gradeLabel,
            List<ReportSection> sections,
            List<ReportNotice> notices,
            List<PriceScenarioPoint> priceScenarios,
            ReportExplanation explanation
    ) {
    }

    public record ReportSection(
            String key,
            String title,
            String description,
            List<ReportMetric> metrics
    ) {
    }

    public record ReportMetric(
            String key,
            String label,
            Object value,
            String unit
    ) {
    }

    public record ReportNotice(
            String code,
            String title,
            String description,
            String severity
    ) {
    }

    public record PriceScenarioPoint(
            String label,
            Integer priceDropRate,
            Long estimatedSalePrice,
            Double collateralBurdenRate,
            String verdict
    ) {
    }

    public record ReportExplanation(
            String summary,
            List<ReportFinding> keyFindings,
            List<String> cautions,
            List<ReportAction> recommendedActions,
            String generatedBy
    ) {
    }

    public record ReportFinding(
            String title,
            String description
    ) {
    }

    public record ReportAction(
            String label,
            String description
    ) {
    }
}

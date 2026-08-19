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
            String grade,
            Breakdown breakdown,
            Weights weights,
            Boolean gradeOverridden,
            Boolean provisionalCollateralBasis
    ) {
    }

    public record Breakdown(
            Integer underwater,
            Integer rollover,
            Integer property,
            Integer market
    ) {
    }

    /** 위험요인별 만점. 클라이언트가 만점을 따로 갖지 않도록 AI가 함께 내려준다. */
    public record Weights(
            Integer underwater,
            Integer rollover,
            Integer property,
            Integer market,
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
            List<String> recommendedActions,
            String generatedBy
    ) {
    }

    public record ReportFinding(
            String title,
            String description
    ) {
    }
}

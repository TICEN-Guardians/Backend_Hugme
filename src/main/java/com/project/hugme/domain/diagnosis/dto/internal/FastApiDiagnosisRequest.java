package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.infra.ocr.entity.LandlordWatchlistCheck;
import com.project.hugme.infra.ocr.entity.RegistryResult;
import com.project.hugme.infra.ocr.enums.CheckStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FastApiDiagnosisRequest(
        Long analysisId,
        String address,
        String dongName,
        String hoName,
        Long deposit,
        LocalDate contractDate,
        BigDecimal contractArea,
        RegistryRisk registryRisk
) {
    public static FastApiDiagnosisRequest from(
            Diagnosis diagnosis,
            RegistryResult registryResult,
            List<LandlordWatchlistCheck> watchlistChecks
    ) {
        RegistryRisk risk = registryResult == null
                ? null
                : RegistryRisk.from(registryResult, watchlistChecks);

        return new FastApiDiagnosisRequest(
                diagnosis.getAnalysisId(),
                diagnosis.getAddress(),
                diagnosis.getDongName(),
                diagnosis.getHoName(),
                diagnosis.getDeposit(),
                diagnosis.getContractDate(),
                diagnosis.getContractArea(),
                risk
        );
    }

    public record RegistryRisk(
            String parseStatus,
            String parseConfidence,
            Long totalActiveMaxClaimAmount,
            String seizure,
            String provisionalSeizure,
            String provisionalDisposition,
            String auctionCommenced,
            String trustRegistration,
            String hasActiveJeonseRight,
            String hasActiveLeaseholdRegistration,
            String ownerMatchesContractParty,
            String watchlistCheckStatus,
            Boolean badLandlordMatched
    ) {
        private static RegistryRisk from(
                RegistryResult result,
                List<LandlordWatchlistCheck> checks
        ) {
            WatchlistSummary watchlist = WatchlistSummary.from(checks);

            return new RegistryRisk(
                    result.getParseStatus().name(),
                    result.getParseConfidence().name(),
                    result.getTotalActiveMaxClaimAmount(),
                    result.getSeizure().name(),
                    result.getProvisionalSeizure().name(),
                    result.getProvisionalDisposition().name(),
                    result.getAuctionCommenced().name(),
                    result.getTrustRegistration().name(),
                    result.getHasActiveJeonseRight().name(),
                    result.getHasActiveLeaseholdRegistration().name(),
                    "UNKNOWN",
                    watchlist.status(),
                    watchlist.matched()
            );
        }
    }

    private record WatchlistSummary(String status, Boolean matched) {
        private static WatchlistSummary from(
                List<LandlordWatchlistCheck> checks
        ) {
            if (checks.stream().anyMatch(check ->
                    check.getCheckStatus() == CheckStatus.CHECKED
                            && Boolean.TRUE.equals(check.getMatched()))) {
                return new WatchlistSummary("CHECKED", true);
            }

            if (!checks.isEmpty() && checks.stream().allMatch(check ->
                    check.getCheckStatus() == CheckStatus.CHECKED)) {
                return new WatchlistSummary("CHECKED", false);
            }

            if (checks.stream().anyMatch(check ->
                    check.getCheckStatus() == CheckStatus.ERROR)) {
                return new WatchlistSummary("ERROR", null);
            }

            return new WatchlistSummary("NOT_CHECKED", null);
        }
    }
}

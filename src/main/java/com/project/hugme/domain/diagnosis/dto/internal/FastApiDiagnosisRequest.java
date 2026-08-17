package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.infra.ocr.entity.LandlordWatchlistCheck;
import com.project.hugme.infra.ocr.entity.RegistryResult;
import com.project.hugme.infra.ocr.entity.RegistryOwner;
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
            List<LandlordWatchlistCheck> watchlistChecks,
            List<RegistryOwner> owners
    ) {
        RegistryRisk risk = registryResult == null
                ? null
                : RegistryRisk.from(
                        registryResult,
                        watchlistChecks,
                        diagnosis.getLandlordName(),
                        owners
                );

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
                List<LandlordWatchlistCheck> checks,
                String landlordName,
                List<RegistryOwner> owners
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
                    ownerMatch(landlordName, owners),
                    watchlist.status(),
                    watchlist.matched()
            );
        }

        private static String ownerMatch(
                String landlordName,
                List<RegistryOwner> owners
        ) {
            String expected = normalizeName(landlordName);
            if (expected == null || owners.isEmpty()) {
                return "UNKNOWN";
            }

            return owners.stream()
                    .map(RegistryOwner::getName)
                    .map(RegistryRisk::normalizeName)
                    .anyMatch(expected::equals)
                    ? "TRUE"
                    : "FALSE";
        }

        private static String normalizeName(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            return value.replaceAll("[^가-힣A-Za-z0-9]", "")
                    .toLowerCase();
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

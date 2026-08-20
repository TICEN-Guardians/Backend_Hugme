package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.infra.ocr.entity.LandlordWatchlistCheck;
import com.project.hugme.infra.ocr.entity.RegistryResult;
import com.project.hugme.infra.ocr.entity.RegistryOwner;
import com.project.hugme.infra.ocr.enums.CheckStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record FastApiDiagnosisRequest(
        Long analysisId,
        String address,
        String dongName,
        String hoName,
        Long deposit,
        LocalDate contractDate,
        BigDecimal contractArea,
        BigDecimal exclusiveArea,
        Integer floor,
        RegistryRisk registryRisk,

        /**
         * 진단 생성 때 받아 둔 주소·건축물대장 정보.
         * AI가 이 값을 쓰면 주소·건축물대장 API를 다시 부르지 않는다.
         * null 이면 AI가 예전처럼 처음부터 다시 조회한다.
         */
        Map<String, Object> propertySnapshot
) {
    public static FastApiDiagnosisRequest from(
            Diagnosis diagnosis,
            RegistryResult registryResult,
            List<LandlordWatchlistCheck> watchlistChecks,
            List<RegistryOwner> owners,
            Map<String, Object> propertySnapshot
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
                diagnosis.getExclusiveArea(),
                diagnosis.getFloor(),
                risk,
                propertySnapshot
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
            String contractPartyName,
            List<String> ownerNames,
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
                    landlordName,
                    ownerNames(owners),
                    watchlist.status(),
                    watchlist.matched()
            );
        }

        private static List<String> ownerNames(List<RegistryOwner> owners) {
            return owners.stream()
                    .map(RegistryOwner::getName)
                    .filter(name -> name != null && !name.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();
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

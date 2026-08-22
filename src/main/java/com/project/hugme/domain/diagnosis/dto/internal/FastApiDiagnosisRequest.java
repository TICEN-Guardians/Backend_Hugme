package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.domain.diagnosis.service.RegistryVerification;
import com.project.hugme.infra.ocr.entity.LandlordWatchlistCheck;
import com.project.hugme.infra.ocr.entity.RegistryOwner;
import com.project.hugme.infra.ocr.entity.RegistryResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record FastApiDiagnosisRequest(
        Long analysisId,
        String mode,
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
                diagnosis.getMode().name(),
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
            RegistryVerification.WatchlistSummary watchlist =
                    RegistryVerification.summarizeWatchlist(checks);

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
                    RegistryVerification.ownerMatchStatus(landlordName, owners),
                    landlordName,
                    RegistryVerification.ownerNames(owners),
                    watchlist.status(),
                    watchlist.matched()
            );
        }
    }
}

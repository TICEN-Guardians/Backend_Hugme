package com.project.hugme.domain.diagnosis.dto.response;

import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.domain.diagnosis.enums.RegistryAddressMatchStatus;
import com.project.hugme.domain.diagnosis.service.RegistryVerification;
import com.project.hugme.infra.ocr.entity.LandlordWatchlistCheck;
import com.project.hugme.infra.ocr.entity.RegistryOwner;
import com.project.hugme.infra.ocr.entity.RegistryResult;
import com.project.hugme.infra.ocr.entity.RegistryRight;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RegistryVerificationResponse(
        LocalDate issueDate,
        String registryAddress,
        String addressMatchStatus,
        boolean addressMatchReviewConfirmed,
        String parseStatus,
        String parseConfidence,
        List<Owner> currentOwners,
        String contractPartyName,
        String ownerMatchStatus,
        String watchlistCheckStatus,
        Boolean badLandlordMatched,
        List<WatchlistCheck> watchlistChecks,
        List<RightEvidence> rightEvidence
) {

    private static final Pattern FILE_PATTERN =
            Pattern.compile("\\[FILE\\s+([^]]+)]");
    private static final Pattern PAGE_PATTERN =
            Pattern.compile("\\[PAGE\\s+(\\d+)]");

    public record Owner(
            String name,
            String share
    ) {
    }

    public record WatchlistCheck(
            String ownerName,
            String checkStatus,
            String matchStatus,
            Boolean matched,
            String matchType,
            Instant checkedAt
    ) {
    }

    public record SourceReference(
            String fileName,
            Integer page
    ) {
    }

    public record RightEvidence(
            String section,
            String rightType,
            String rankNo,
            String holder,
            String debtor,
            Long amount,
            String status,
            List<SourceReference> sources
    ) {
    }

    public static RegistryVerificationResponse from(
            Diagnosis diagnosis,
            RegistryResult result,
            List<RegistryRight> rights,
            List<RegistryOwner> owners,
            List<LandlordWatchlistCheck> checks,
            RegistryAddressMatchStatus addressMatchStatus
    ) {
        if (result == null) {
            return null;
        }

        RegistryVerification.WatchlistSummary watchlist =
                RegistryVerification.summarizeWatchlist(checks);

        List<Owner> currentOwners = owners.stream()
                .map(owner -> new Owner(owner.getName(), owner.getShare()))
                .toList();

        List<WatchlistCheck> watchlistChecks = checks.stream()
                .map(check -> new WatchlistCheck(
                        check.getRegistryOwner() == null
                                ? null
                                : check.getRegistryOwner().getName(),
                        name(check.getCheckStatus()),
                        name(check.getMatchStatus()),
                        check.getMatched(),
                        name(check.getMatchType()),
                        check.getCheckedAt()
                ))
                .toList();

        List<RightEvidence> rightEvidence = rights.stream()
                .map(right -> new RightEvidence(
                        name(right.getSection()),
                        name(right.getRightType()),
                        right.getRankNo(),
                        right.getHolder(),
                        right.getDebtor(),
                        right.getAmount(),
                        name(right.getStatus()),
                        sourceReferences(right.getRawText())
                ))
                .toList();

        return new RegistryVerificationResponse(
                result.getIssueDate(),
                result.getRawAddress(),
                addressMatchStatus.name(),
                diagnosis.isRegistryAddressReviewConfirmed(),
                name(result.getParseStatus()),
                name(result.getParseConfidence()),
                currentOwners,
                diagnosis.getLandlordName(),
                RegistryVerification.ownerMatchStatus(
                        diagnosis.getLandlordName(),
                        owners
                ),
                watchlist.status(),
                watchlist.matched(),
                watchlistChecks,
                rightEvidence
        );
    }

    private static List<SourceReference> sourceReferences(String rawText) {
        String evidence = rawText == null ? "" : rawText;
        Matcher fileMatcher = FILE_PATTERN.matcher(evidence);
        String fileName = fileMatcher.find()
                ? fileMatcher.group(1).trim()
                : null;
        Matcher pageMatcher = PAGE_PATTERN.matcher(evidence);
        LinkedHashSet<Integer> pages = new LinkedHashSet<>();
        while (pageMatcher.find()) {
            pages.add(Integer.valueOf(pageMatcher.group(1)));
        }
        return pages.stream()
                .map(page -> new SourceReference(fileName, page))
                .toList();
    }

    private static String name(Enum<?> value) {
        return value == null ? null : value.name();
    }
}

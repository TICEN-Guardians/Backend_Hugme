package com.project.hugme.domain.diagnosis.service;

import com.project.hugme.infra.ocr.entity.LandlordWatchlistCheck;
import com.project.hugme.infra.ocr.entity.RegistryOwner;
import com.project.hugme.infra.ocr.enums.CheckStatus;

import java.util.List;
import java.util.Locale;

public final class RegistryVerification {

    private RegistryVerification() {
    }

    public static List<String> ownerNames(List<RegistryOwner> owners) {
        return owners.stream()
                .map(RegistryOwner::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    public static String ownerMatchStatus(
            String landlordName,
            List<RegistryOwner> owners
    ) {
        String expected = normalizeName(landlordName);
        List<String> currentOwnerNames = ownerNames(owners);
        if (expected == null || currentOwnerNames.isEmpty()) {
            return "UNKNOWN";
        }

        return currentOwnerNames.stream()
                .map(RegistryVerification::normalizeName)
                .anyMatch(expected::equals)
                ? "TRUE"
                : "FALSE";
    }

    public static WatchlistSummary summarizeWatchlist(
            List<LandlordWatchlistCheck> checks
    ) {
        if (checks.stream().anyMatch(check ->
                check.getCheckStatus() == CheckStatus.CHECKED
                        && Boolean.TRUE.equals(check.getMatched()))) {
            return new WatchlistSummary("CHECKED", true);
        }

        if (!checks.isEmpty() && checks.stream().allMatch(check ->
                check.getCheckStatus() == CheckStatus.CHECKED)) {
            boolean allConfirmedNoMatch = checks.stream().allMatch(check ->
                    Boolean.FALSE.equals(check.getMatched()));
            return new WatchlistSummary(
                    "CHECKED",
                    allConfirmedNoMatch ? false : null
            );
        }

        if (checks.stream().anyMatch(check ->
                check.getCheckStatus() == CheckStatus.ERROR)) {
            return new WatchlistSummary("ERROR", null);
        }

        return new WatchlistSummary("NOT_CHECKED", null);
    }

    private static String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.replaceAll("[^가-힣A-Za-z0-9]", "")
                .toLowerCase(Locale.ROOT);
    }

    public record WatchlistSummary(String status, Boolean matched) {
    }
}

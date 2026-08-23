package com.project.hugme.domain.diagnosis.service;

import com.project.hugme.domain.diagnosis.enums.RegistryAddressMatchStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RegistryAddressMatchService {

    private static final Pattern PARENTHESIZED = Pattern.compile("\\([^)]*\\)");
    private static final Pattern DONG = Pattern.compile("제([가-힣A-Za-z0-9]+)동");
    private static final Pattern HO = Pattern.compile("제([A-Za-z0-9]+)호");
    private static final Map<String, String> LETTER_NAMES = Map.ofEntries(
            Map.entry("A", "에이"),
            Map.entry("B", "비"),
            Map.entry("C", "씨"),
            Map.entry("D", "디"),
            Map.entry("E", "이"),
            Map.entry("F", "에프")
    );

    public RegistryAddressMatchStatus match(
            String confirmedAddress,
            String dongName,
            String hoName,
            Map<String, Object> propertySnapshot,
            String registryAddress
    ) {
        if (confirmedAddress == null || confirmedAddress.isBlank()) {
            return RegistryAddressMatchStatus.PENDING_ADDRESS_CONFIRMATION;
        }
        if (registryAddress == null || registryAddress.isBlank()) {
            return RegistryAddressMatchStatus.UNREADABLE;
        }

        String normalizedRegistry = normalizeAddress(registryAddress);
        boolean baseMatches = addressCandidates(
                confirmedAddress,
                propertySnapshot
        ).stream().map(this::normalizeAddress)
                .filter(candidate -> !candidate.isBlank())
                .anyMatch(normalizedRegistry::contains);
        if (!baseMatches) {
            return RegistryAddressMatchStatus.MISMATCH;
        }

        RegistryAddressMatchStatus dongStatus = matchDong(
                dongName,
                extractUnits(registryAddress, DONG),
                registryAddress
        );
        RegistryAddressMatchStatus hoStatus = matchUnit(
                hoName,
                extractUnits(registryAddress, HO)
        );
        if (dongStatus == RegistryAddressMatchStatus.MISMATCH
                || hoStatus == RegistryAddressMatchStatus.MISMATCH) {
            return RegistryAddressMatchStatus.MISMATCH;
        }
        if (dongStatus == RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED
                || hoStatus == RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED) {
            return RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED;
        }
        return RegistryAddressMatchStatus.MATCH;
    }

    private List<String> addressCandidates(
            String confirmedAddress,
            Map<String, Object> propertySnapshot
    ) {
        List<String> candidates = new ArrayList<>();
        candidates.add(confirmedAddress);
        if (propertySnapshot != null) {
            addCandidate(candidates, propertySnapshot.get("roadAddress"));
            addCandidate(candidates, propertySnapshot.get("jibunAddress"));
        }
        return candidates;
    }

    private void addCandidate(List<String> candidates, Object value) {
        if (value != null && !String.valueOf(value).isBlank()) {
            candidates.add(String.valueOf(value));
        }
    }

    private RegistryAddressMatchStatus matchUnit(
            String expected,
            Set<String> actual
    ) {
        String normalizedExpected = normalizeUnit(expected);
        if (normalizedExpected == null) {
            return actual.isEmpty()
                    ? RegistryAddressMatchStatus.MATCH
                    : RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED;
        }
        if (actual.isEmpty()) {
            return RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED;
        }
        return actual.contains(normalizedExpected)
                ? RegistryAddressMatchStatus.MATCH
                : RegistryAddressMatchStatus.MISMATCH;
    }

    private RegistryAddressMatchStatus matchDong(
            String expected,
            Set<String> actual,
            String registryAddress
    ) {
        String normalizedExpected = normalizeUnit(expected);
        if (normalizedExpected == null) {
            return actual.isEmpty()
                    ? RegistryAddressMatchStatus.MATCH
                    : RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED;
        }
        if (actual.contains(normalizedExpected)) {
            return RegistryAddressMatchStatus.MATCH;
        }
        if (!actual.isEmpty()) {
            return RegistryAddressMatchStatus.MISMATCH;
        }

        boolean buildingNameMatches = !normalizedExpected.matches("\\d+")
                && normalizeAddress(registryAddress).contains(
                normalizedExpected.toLowerCase(Locale.ROOT)
        );
        return buildingNameMatches
                ? RegistryAddressMatchStatus.MATCH
                : RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED;
    }

    private Set<String> extractUnits(String value, Pattern pattern) {
        Matcher matcher = pattern.matcher(value);
        Set<String> units = new java.util.HashSet<>();
        while (matcher.find()) {
            String normalized = normalizeUnit(matcher.group(1));
            if (normalized != null) {
                units.add(normalized);
            }
        }
        return units;
    }

    private String normalizeAddress(String value) {
        String withoutParentheses = PARENTHESIZED.matcher(value).replaceAll("");
        return withoutParentheses
                .replaceAll("^\\[[^]]+\\]", "")
                .replaceAll("[^가-힣A-Za-z0-9]", "")
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeUnit(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value
                .replace("제", "")
                .replaceAll("(동|층|호)$", "")
                .replaceAll("[^가-힣A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
        normalized = LETTER_NAMES.getOrDefault(normalized, normalized);
        if (normalized.matches("\\d+")) {
            normalized = normalized.replaceFirst("^0+(?!$)", "");
        }
        return normalized.isBlank() ? null : normalized;
    }
}

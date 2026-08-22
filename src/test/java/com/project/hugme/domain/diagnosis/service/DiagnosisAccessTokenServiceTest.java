package com.project.hugme.domain.diagnosis.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosisAccessTokenServiceTest {

    private final DiagnosisAccessTokenService service =
            new DiagnosisAccessTokenService();

    @Test
    void issuedTokenMatchesOnlyItsStoredHash() {
        DiagnosisAccessTokenService.IssuedToken first = service.issue();
        DiagnosisAccessTokenService.IssuedToken second = service.issue();

        assertNotEquals(first.value(), second.value());
        assertTrue(service.matches(first.value(), first.hash()));
        assertFalse(service.matches(second.value(), first.hash()));
    }
}

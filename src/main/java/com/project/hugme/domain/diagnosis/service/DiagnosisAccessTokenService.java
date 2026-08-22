package com.project.hugme.domain.diagnosis.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Component
public class DiagnosisAccessTokenService {

    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private final SecureRandom secureRandom = new SecureRandom();

    public IssuedToken issue() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new IssuedToken(value, hash(value), Instant.now().plus(TOKEN_TTL));
    }

    public boolean matches(String value, String expectedHash) {
        if (value == null || value.isBlank() || expectedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(value).getBytes(StandardCharsets.US_ASCII),
                expectedHash.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", exception);
        }
    }

    public record IssuedToken(
            String value,
            String hash,
            Instant expiresAt
    ) {
    }
}

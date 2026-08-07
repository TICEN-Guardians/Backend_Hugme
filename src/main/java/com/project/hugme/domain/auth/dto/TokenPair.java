package com.project.hugme.domain.auth.dto;

// Service → Controller 전달용
public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
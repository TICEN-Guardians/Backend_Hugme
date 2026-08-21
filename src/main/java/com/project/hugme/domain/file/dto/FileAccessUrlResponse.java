package com.project.hugme.domain.file.dto;

public record FileAccessUrlResponse(
        String url,
        long expiresIn
) {
}

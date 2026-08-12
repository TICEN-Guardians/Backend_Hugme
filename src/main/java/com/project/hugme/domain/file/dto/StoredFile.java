package com.project.hugme.domain.file.dto;

public record StoredFile(
        String originalFileName,
        String storedFileName,
        String storageKey,
        String mimeType,
        Long fileSize
) {
}

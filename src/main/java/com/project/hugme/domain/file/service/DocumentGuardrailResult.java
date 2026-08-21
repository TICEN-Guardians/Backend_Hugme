package com.project.hugme.domain.file.service;

import com.project.hugme.domain.file.entity.DocumentValidationStatus;

public record DocumentGuardrailResult(
        DocumentValidationStatus status,
        String detectedDocumentType,
        Double confidence,
        String message
) {
}

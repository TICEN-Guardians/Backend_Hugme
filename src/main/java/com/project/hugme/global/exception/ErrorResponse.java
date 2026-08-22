package com.project.hugme.global.exception;

public record ErrorResponse(
        int status,
        String code,
        String message
) {
}
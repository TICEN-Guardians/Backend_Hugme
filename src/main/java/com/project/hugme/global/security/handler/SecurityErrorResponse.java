package com.project.hugme.global.security.handler;

public record SecurityErrorResponse(
        int status,
        String code,
        String message
) {
}
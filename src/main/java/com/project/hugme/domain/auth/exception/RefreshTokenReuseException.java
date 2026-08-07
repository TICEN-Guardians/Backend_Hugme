package com.project.hugme.domain.auth.exception;

public class RefreshTokenReuseException extends RuntimeException {

    public RefreshTokenReuseException() {
        super("이미 폐기된 Refresh Token입니다.");
    }
}
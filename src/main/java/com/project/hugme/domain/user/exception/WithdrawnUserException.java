package com.project.hugme.domain.user.exception;

public class WithdrawnUserException extends RuntimeException {

    public WithdrawnUserException() {
        super("이미 탈퇴한 사용자입니다.");
    }
}
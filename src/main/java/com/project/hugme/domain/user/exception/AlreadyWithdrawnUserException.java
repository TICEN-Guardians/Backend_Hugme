package com.project.hugme.domain.user.exception;

public class AlreadyWithdrawnUserException extends RuntimeException {

    public AlreadyWithdrawnUserException() {
        super("이미 탈퇴한 사용자입니다.");
    }
}
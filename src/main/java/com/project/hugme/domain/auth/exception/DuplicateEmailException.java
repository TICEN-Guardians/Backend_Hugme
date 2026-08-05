package com.project.hugme.domain.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends RuntimeException{

    public DuplicateEmailException(){
        super("이미 사용 중인 이메일입니다.");
    }
}

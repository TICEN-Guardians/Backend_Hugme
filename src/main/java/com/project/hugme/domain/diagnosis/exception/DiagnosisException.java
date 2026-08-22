package com.project.hugme.domain.diagnosis.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DiagnosisException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public DiagnosisException(
            HttpStatus status,
            String code,
            String message
    ) {
        super(message);
        this.status = status;
        this.code = code;
    }
}

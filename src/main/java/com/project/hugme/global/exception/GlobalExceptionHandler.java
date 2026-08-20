package com.project.hugme.global.exception;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.project.hugme.domain.auth.exception.InvalidCredentialsException;
import com.project.hugme.domain.auth.exception.RefreshTokenReuseException;
import com.project.hugme.domain.user.exception.WithdrawnUserException;
import com.project.hugme.domain.user.exception.EmailVerificationRequiredException;
import com.project.hugme.domain.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * 존재하지 않는 사용자
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException exception
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND",
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(EmailVerificationRequiredException.class)
    public ResponseEntity<ErrorResponse> handleEmailVerificationRequired(
            EmailVerificationRequiredException e
    ) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        403,
                        "EMAIL_VERIFICATION_REQUIRED",
                        e.getMessage()
                ));
    }


    /**
     * 이미 탈퇴한 사용자
     */
    @ExceptionHandler(WithdrawnUserException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyWithdrawnUserException(
            WithdrawnUserException exception
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "ALREADY_WITHDRAWN_USER",
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(RefreshTokenReuseException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenReuse(
            RefreshTokenReuseException e
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        401,
                        "REFRESH_TOKEN_REUSED",
                        e.getMessage()
                ));
    }

    /**
     * AI(FastAPI) 서버가 오류 응답을 준 경우.
     *
     * AI 는 사용자가 고칠 수 있는 문제와 외부 API 장애를 상태코드로 구분해 보낸다.
     *   4xx  진단 요청 자체가 성립하지 않음 (주소 다건, 동 정보 누락 등)
     *        -> 사용자가 보고 조치할 수 있으므로 코드와 메시지를 그대로 전달한다.
     *   5xx  AI 내부 오류이거나 공공 API 장애
     *        -> 상세 내용은 로그에만 남기고 사용자에게는 일반 문구를 준다.
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleAiResponse(
            RestClientResponseException exception
    ) {
        HttpStatusCode statusCode = exception.getStatusCode();
        String body = exception.getResponseBodyAsString();

        if (statusCode.is5xxServerError()) {
            log.error(
                    "AI 서버 오류 status={} body={}",
                    statusCode.value(),
                    body,
                    exception
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse(
                            HttpStatus.BAD_GATEWAY.value(),
                            "AI_SERVICE_ERROR",
                            "분석 서버와 통신하지 못했습니다. 잠시 후 다시 시도해 주세요."
                    ));
        }

        String code = detailField(body, "code", "AI_REQUEST_REJECTED");
        String message = detailField(
                body,
                "message",
                "요청을 처리할 수 없습니다."
        );

        log.warn(
                "AI 요청 거절 status={} code={} message={}",
                statusCode.value(),
                code,
                message
        );

        return ResponseEntity
                .status(statusCode)
                .body(new ErrorResponse(
                        statusCode.value(),
                        code,
                        message
                ));
    }

    /**
     * AI 서버에 연결하지 못했거나 읽기 제한시간을 넘긴 경우.
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleAiUnreachable(
            ResourceAccessException exception
    ) {
        log.error("AI 서버 연결 실패", exception);

        return ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse(
                        HttpStatus.GATEWAY_TIMEOUT.value(),
                        "AI_SERVICE_TIMEOUT",
                        "분석 서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요."
                ));
    }

    /**
     * FastAPI 오류 본문은 {"detail": {"code": ..., "message": ...}} 형태다.
     * 검증 실패 등에서는 detail 이 문자열이나 배열일 수 있어 기본값으로 되돌린다.
     */
    private String detailField(
            String body,
            String name,
            String fallback
    ) {
        if (body == null || body.isBlank()) {
            return fallback;
        }

        try {
            JsonNode detail = objectMapper.readTree(body).path("detail");
            JsonNode value = detail.path(name);

            if (value.isTextual() && !value.asText().isBlank()) {
                return value.asText();
            }
        } catch (Exception exception) {
            log.debug("AI 오류 본문 해석 실패: {}", body);
        }

        return fallback;
    }

    /**
     * 처리되지 않은 서버 내부 오류
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception
    ) {
        log.error("Unhandled exception", exception);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(
            InvalidCredentialsException.class
    )
    public ResponseEntity<ErrorResponse>
    handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        "INVALID_CREDENTIALS",
                        exception.getMessage()
                ));
    }
}
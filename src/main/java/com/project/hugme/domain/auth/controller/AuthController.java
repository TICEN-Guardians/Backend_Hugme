package com.project.hugme.domain.auth.controller;

import com.project.hugme.domain.auth.dto.*;
import com.project.hugme.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "인증 API",
        description = "회원가입, 로그인, 이메일 인증 및 JWT 토큰 관리 API"
)
public class AuthController {

    private final AuthService authService;

    @SecurityRequirements
    @Operation(
            summary = "1. 회원가입",
            description = "일반 회원가입을 진행하고 이메일 인증 메일을 발송합니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(
            @Valid @RequestBody SignUpRequest req
    ) {
        SignUpResponse response = authService.signUp(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @SecurityRequirements
    @Operation(
            summary = "2. 이메일 중복 확인",
            description = "회원가입 시 이메일 중복 여부를 확인합니다."
    )
    @PostMapping("/mail/check")
    public ResponseEntity<Boolean> checkEmail(
            @RequestBody EmailCheckRequest request
    ) {
        return ResponseEntity.ok(
                authService.isEmailDuplicated(request.mail())
        );

    }



    @SecurityRequirements
    @Operation(
            summary = "3. 이메일 인증",
            description = "이메일로 전달된 인증 토큰을 검증하고 회원 상태를 ACTIVE로 변경합니다."
    )
    @GetMapping("/mail/verify")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token) {
        authService.verifyEmail(token);

        return ResponseEntity.ok(
                "이메일 인증이 완료되었습니다."
        );

    }


    @SecurityRequirements
    @Operation(
            summary = "4. 로그인",
            description = "이메일과 비밀번호로 로그인합니다. " +
                    "Access Token은 응답 Body로 반환하고, " +
                    "Refresh Token은 HttpOnly Cookie로 발급합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenPair tokenPair = authService.login(request);

        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", tokenPair.refreshToken())
                .httpOnly(true)
                .secure(true)    //http 기준- false
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(LoginResponse.of(tokenPair.accessToken()));
    }

    @Operation(
            summary = "5. Access Token 재발급",
            description = "HttpOnly Cookie의 Refresh Token을 검증하고 " +
                    "새로운 Access Token과 Refresh Token을 발급합니다."
    )
    @PostMapping("/token/reissue")
    public ResponseEntity<TokenReissueResponse> reissue(
            @Parameter(hidden = true)
            @CookieValue("refreshToken") String refreshToken
    ) {
        TokenPair tokenPair =
                authService.reissue(refreshToken);

        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", tokenPair.refreshToken())
                .httpOnly(true)
                .secure(true)   // 로컬 HTTP
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(
                        TokenReissueResponse.of(tokenPair.accessToken())

                );
    }


    @Operation(
            summary = "6. 로그아웃",
            description = "DB에 저장된 Refresh Token을 폐기하고 HttpOnly Cookie를 삭제합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        authService.logout(userId);

        // 브라우저 Refresh Token Cookie 삭제
        ResponseCookie deleteCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(true)       // 로컬 HTTP
                .path("/api/auth")   // 생성할 때와 동일해야 함
                .maxAge(0)           // 즉시 만료 = 삭제
                .build();

        return ResponseEntity.noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        deleteCookie.toString()
                )
                .build();
    }


}

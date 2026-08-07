package com.project.hugme.domain.auth.controller;

import com.project.hugme.domain.auth.dto.*;
import com.project.hugme.domain.auth.security.CustomUserDetails;
import com.project.hugme.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
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
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(
            @Valid @RequestBody SignUpRequest req
            ){
        SignUpResponse response = authService.signUp(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ){
        TokenPair tokenPair = authService.login(request);

        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", tokenPair.refreshToken())
                .httpOnly(true)
                .secure(false)   //http 기준
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(LoginResponse.of(tokenPair.accessToken()));
    }

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
                .secure(false)   // 로컬 HTTP
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

    @GetMapping("/mail/verify")
    public ResponseEntity<String> verifyEmail(
@RequestParam String token ){
        authService.verifyEmail(token);

        return ResponseEntity.ok(
                "이메일 인증이 완료되었습니다."
        );

    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ){
        authService.logout(userId);

        // 브라우저 Refresh Token Cookie 삭제
        ResponseCookie deleteCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(false)       // 로컬 HTTP
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

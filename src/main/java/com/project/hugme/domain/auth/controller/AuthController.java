package com.project.hugme.domain.auth.controller;

import com.project.hugme.domain.auth.dto.LoginRequest;
import com.project.hugme.domain.auth.dto.LoginResponse;
import com.project.hugme.domain.auth.dto.SignUpRequest;
import com.project.hugme.domain.auth.dto.SignUpResponse;
import com.project.hugme.domain.auth.security.CustomUserDetails;
import com.project.hugme.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);

    }






}

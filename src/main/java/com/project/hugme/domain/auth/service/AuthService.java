package com.project.hugme.domain.auth.service;


import com.project.hugme.domain.auth.dto.LoginRequest;
import com.project.hugme.domain.auth.dto.SignUpRequest;
import com.project.hugme.domain.auth.dto.SignUpResponse;
import com.project.hugme.domain.auth.dto.TokenPair;
import com.project.hugme.domain.auth.entity.RefreshToken;
import com.project.hugme.domain.auth.exception.DuplicateEmailException;
import com.project.hugme.domain.auth.exception.InvalidCredentialsException;
import com.project.hugme.domain.auth.exception.RefreshTokenReuseException;
import com.project.hugme.domain.auth.repository.RefreshTokenRepository;
import com.project.hugme.domain.auth.security.CustomUserDetails;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.entity.UserStatus;
import com.project.hugme.domain.user.exception.EmailVerificationRequiredException;
import com.project.hugme.domain.user.exception.UserNotFoundException;
import com.project.hugme.domain.user.exception.WithdrawnUserException;
import com.project.hugme.domain.user.repository.UserRepository;
import com.project.hugme.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;


    private final RefreshTokenRevocationService refreshTokenRevocationService;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        String verificationToken =
                UUID.randomUUID().toString();

        String verificationUrl =
                "https://hugm3.com/api/auth/mail/verify?token="
                        + verificationToken;


        User user = User.createLocalUser(
                request.email(),
                encodedPassword,
                request.name(),
                verificationToken
        );


        User savedUser = userRepository.save(user);

        emailService.sendVerificationEmail(
                user.getEmail(),
                verificationUrl
        );


        return SignUpResponse.from(savedUser);
    }

    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public TokenPair login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        if (user.getStatus() == UserStatus.PENDING) {
            throw new EmailVerificationRequiredException();
        }

        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new WithdrawnUserException();
        }
        Authentication authentication;
        try {
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.email(),
                                    request.password()
                            )
                    );
        } catch (AuthenticationException exception) {
            /*
             * 비밀번호 불일치 등 로그인 인증 실패
             */
            throw new InvalidCredentialsException();
        }



        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(userDetails);

        String refreshToken =
                jwtTokenProvider.createRefreshToken(userDetails);

        Instant expiresAt =
                jwtTokenProvider.getRefreshTokenExpiresAt();


        saveRefreshToken(user, refreshToken, expiresAt);

        return new TokenPair(accessToken, refreshToken);

    }

    private void saveRefreshToken(
            User user,
            String tokenValue,
            Instant expiresAt
    ) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByUserUserId(user.getUserId())
                .map(savedToken -> {
                    savedToken.update(tokenValue, expiresAt);
                    return savedToken;
                })
                .orElseGet(() ->
                        RefreshToken.create(
                                user,
                                tokenValue,
                                expiresAt
                        )
                );

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public TokenPair reissue(
            String requestToken
    ) {
        // 1. 서명, 만료시간, 토큰 타입 검증
        jwtTokenProvider.validateRefreshToken(requestToken);

        // 2. Refresh Token에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserId(requestToken);

        // 3. DB에 저장된 현재 Refresh Token 조회
        RefreshToken savedToken = refreshTokenRepository.findByUserUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("저장된 Refresh Token이 없습니다."));


        // 5. 현재 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        if (!savedToken.getTokenValue().equals(requestToken)) {

            // 현재 유효한 Refresh Token까지 폐기
            refreshTokenRevocationService.revoke(userId);

            // 보안 알림 메일 발송
            try {
                emailService.sendRefreshTokenReuseAlert(
                        user.getEmail()
                );
            } catch (Exception e) {
                log.warn(
                        "Refresh Token 재사용 알림 이메일 발송 실패. userId={}",
                        userId,
                        e
                );
            }

            throw new RefreshTokenReuseException();
        }


        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "비활성화된 사용자입니다."
            );
        }

        CustomUserDetails userDetails = CustomUserDetails.from(user);

        String newAccessToken = jwtTokenProvider.createAccessToken(userDetails);

        String newRefreshToken = jwtTokenProvider.createRefreshToken(userDetails);

        savedToken.update(newRefreshToken, jwtTokenProvider.getRefreshTokenExpiresAt());

        return new TokenPair(newAccessToken, newRefreshToken);

    }

    @Transactional
    public void verifyEmail(String token) {

        User user = userRepository
                .findByVerificationToken(token)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "유효하지 않은 이메일 인증 요청입니다."
                        )
                );

        user.verifyEmail();
    }

    @Transactional
    public void logout(Long userId) {

        refreshTokenRepository.deleteByUserUserId(userId);
    }


}

package com.project.hugme.domain.auth.service;


import com.project.hugme.domain.auth.dto.*;
import com.project.hugme.domain.auth.entity.RefreshToken;
import com.project.hugme.domain.auth.exception.DuplicateEmailException;
import com.project.hugme.domain.auth.exception.RefreshTokenReuseException;
import com.project.hugme.domain.auth.repository.RefreshTokenRepository;
import com.project.hugme.domain.auth.security.CustomUserDetails;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.entity.UserStatus;
import com.project.hugme.domain.user.exception.UserNotFoundException;
import com.project.hugme.domain.user.repository.UserRepository;
import com.project.hugme.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
private final RefreshTokenRevocationService refreshTokenRevocationService;
    @Transactional
    public SignUpResponse signUp(SignUpRequest request)  {

        if(userRepository.existsByEmail(request.email())){
            throw new DuplicateEmailException();
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.createLocalUser(
                request.email(),
                encodedPassword,
                request.name()
        );

        User savedUser = userRepository.save(user);

        return SignUpResponse.from(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest request){

        Authentication authentication =
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )

        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(userDetails);

        String refreshToken =
                jwtTokenProvider.createRefreshToken(userDetails);

        Instant expiresAt =
                jwtTokenProvider.getRefreshTokenExpiresAt();

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(UserNotFoundException::new);

        saveRefreshToken(user, refreshToken, expiresAt);

        return LoginResponse.of(

                accessToken,
                refreshToken
        );

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
    public TokenReissueResponse reissue(
            TokenReissueRequest request
    ){
        String requestToken = request.refreshToken();
        // 1. 서명, 만료시간, 토큰 타입 검증
        jwtTokenProvider.validateRefreshToken(requestToken);

        // 2. Refresh Token에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserId(requestToken);

        // 3. DB에 저장된 현재 Refresh Token 조회
        RefreshToken savedToken = refreshTokenRepository.findByUserUserId(userId)
                .orElseThrow(()-> new IllegalArgumentException("저장된 Refresh Token이 없습니다."));

        if (!savedToken.getTokenValue().equals(requestToken)) {

            // 현재 유효한 Refresh Token까지 폐기
            refreshTokenRevocationService.revoke(userId);

            throw new RefreshTokenReuseException();
        }

        // 5. 현재 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        // 6. 탈퇴 사용자 확인
        if (user.getStatus() != UserStatus.ACTIVE) {


            throw new IllegalArgumentException(
                    "비활성화된 사용자입니다."
            );
        }

        CustomUserDetails userDetails = CustomUserDetails.from(user);

        String newAccessToken = jwtTokenProvider.createAccessToken(userDetails);

        String newRefreshToken = jwtTokenProvider.createRefreshToken(userDetails);

        savedToken.update(newRefreshToken,jwtTokenProvider.getRefreshTokenExpiresAt());

                return TokenReissueResponse.of(
                        newAccessToken,
                        newRefreshToken

                );

    }


}

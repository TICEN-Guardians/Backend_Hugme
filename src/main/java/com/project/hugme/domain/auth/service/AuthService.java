package com.project.hugme.domain.auth.service;


import com.project.hugme.domain.auth.dto.LoginRequest;
import com.project.hugme.domain.auth.dto.LoginResponse;
import com.project.hugme.domain.auth.dto.SignUpRequest;
import com.project.hugme.domain.auth.dto.SignUpResponse;
import com.project.hugme.domain.auth.entity.RefreshToken;
import com.project.hugme.domain.auth.exception.DuplicateEmailException;
import com.project.hugme.domain.auth.repository.RefreshTokenRepository;
import com.project.hugme.domain.auth.security.CustomUserDetails;
import com.project.hugme.domain.user.entity.User;
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


}

package com.project.hugme.domain.auth.repository;

import com.project.hugme.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserUserId(Long userId);

    Optional<RefreshToken> findByTokenValue(String tokenValue);

    void deleteByUserUserId(Long userId);
}
package com.project.hugme.global.security.jwt;

import com.project.hugme.domain.auth.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}")String secret,
            @Value("${jwt.access-token-expiration}")long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ){
byte[] keyBytes = Decoders.BASE64.decode(secret);

this.secretKey = Keys.hmacShaKeyFor(keyBytes);
this.accessTokenExpiration=accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;

    }
//accesstoken 생성
    public String createAccessToken(CustomUserDetails userDetails){
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusMillis(accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userDetails.getUserId()))
                .claim("role",userDetails.getRole().name())
                .claim("tokenType", "ACCESS")
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();


    }
    //refreshtoken 생성
    public String createRefreshToken(CustomUserDetails userDetails) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusMillis(refreshTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userDetails.getUserId()))
                .claim("tokenType", "REFRESH")
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }
    //refreshtoken 새 만료시간 발급
    public Instant getRefreshTokenExpiresAt() {
        return Instant.now().plusMillis(refreshTokenExpiration);
    }
//refreshtoken 타입 확인
    public String getTokenType(String token) {
        Claims claims = parseClaims(token);

        return claims.get("tokenType", String.class);
    }
//token에 대한 userId 확인
    public Long getUserId(String token){
        Claims claims = parseClaims(token);

        return Long.valueOf(claims.getSubject());
    }
    //token에 대한 role 확인
    public String getRole(String token){
        Claims claims = parseClaims(token);

        return claims.get("role", String.class);
    }
    //유효한 토큰인지 확인
    public boolean validateToken(String token){
        try{
            parseClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException exception){
            return false;
        }
    }
//refreshtoken인지 확인
    public void validateRefreshToken(String token) {
        Claims claims = parseClaims(token);

        String tokenType = claims.get("tokenType", String.class);

        if (!"REFRESH".equals(tokenType)) {
            throw new IllegalArgumentException(
                    "Refresh Token이 아닙니다."
            );
        }
    }


    //비밀키 서명 확인, payload의 각 claims 부분 반환
    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}

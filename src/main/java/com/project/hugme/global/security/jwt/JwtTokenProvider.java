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

    public Instant getRefreshTokenExpiresAt() {
        return Instant.now().plusMillis(refreshTokenExpiration);
    }

    public String getTokenType(String token) {
        Claims claims = parseClaims(token);

        return claims.get("tokenType", String.class);
    }

    public Long getUserId(String token){
        Claims claims = parseClaims(token);

        return Long.valueOf(claims.getSubject());
    }

    public String getRole(String token){
        Claims claims = parseClaims(token);

        return claims.get("role", String.class);
    }

    public boolean validateToken(String token){
        try{
            parseClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException exception){
            return false;
        }
    }

    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}

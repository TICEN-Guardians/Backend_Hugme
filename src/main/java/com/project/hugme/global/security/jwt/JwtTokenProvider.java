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

    public JwtTokenProvider(
            @Value("${jwt.secret}")String secret,
            @Value("${jwt.access-token-expiration}")long accessTokenExpiration
    ){
byte[] keyBytes = Decoders.BASE64.decode(secret);

this.secretKey = Keys.hmacShaKeyFor(keyBytes);
this.accessTokenExpiration=accessTokenExpiration;

    }

    public String createAccessToken(CustomUserDetails userDetails){
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusMillis(accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userDetails.getUserId()))
                .claim("role",userDetails.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();


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

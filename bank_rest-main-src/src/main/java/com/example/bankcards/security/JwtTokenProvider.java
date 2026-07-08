package com.example.bankcards.security;

import com.example.bankcards.entity.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${app.jwt.access-token-secret}") String accessSecret,
            @Value("${app.jwt.refresh-token-secret}") String refreshSecret,
            @Value("${app.jwt.access-token-expiration}") String accessExpiration,
            @Value("${app.jwt.refresh-token-expiration}") String refreshExpiration) {

        this.accessTokenKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = parseExpiration(accessExpiration);
        this.refreshTokenExpiration = parseExpiration(refreshExpiration);
    }

    public String generateAccessToken(UUID userId, Role role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(accessTokenKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(refreshTokenKey)
                .compact();
    }

    public UUID getUserIdFromAccessToken(String token) {
        Claims claims = parseAccessToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public Role getRoleFromAccessToken(String token) {
        Claims claims = parseAccessToken(token);
        return Role.valueOf(claims.get("role", String.class));
    }

    public boolean validateAccessToken(String token) {
        try {
            parseAccessToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UUID validateRefreshTokenAndGetUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(refreshTokenKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpiration;
    }

    private Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(accessTokenKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private long parseExpiration(String expiration) {
        if (expiration.endsWith("m")) {
            return Long.parseLong(expiration.replace("m", "")) * 60 * 1000;
        } else if (expiration.endsWith("h")) {
            return Long.parseLong(expiration.replace("h", "")) * 60 * 60 * 1000;
        } else if (expiration.endsWith("d")) {
            return Long.parseLong(expiration.replace("d", "")) * 24 * 60 * 60 * 1000;
        }
        return Long.parseLong(expiration) * 1000;
    }
}


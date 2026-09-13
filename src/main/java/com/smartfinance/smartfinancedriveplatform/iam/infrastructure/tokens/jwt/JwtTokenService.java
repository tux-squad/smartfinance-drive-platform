package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for generating and validating JWT access, refresh & reset tokens using JJWT.
 */
@Service
public class JwtTokenService implements TokenService {

    @Value("${authorization.jwt.secret:SmartFinanceDrivePlatformSecretKeyForJwtTokenGenerationAndValidation2026!}")
    private String secret;

    @Value("${authorization.jwt.expiration-ms:900000}") // 15 minutes default
    private long expirationMs;

    @Value("${authorization.jwt.refresh-expiration-ms:604800000}") // 7 days default
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(String username, List<String> roles) {
        return generateToken(null, username, roles);
    }

    @Override
    public String generateToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("roles", roles)
                .claim("type", "access");

        if (userId != null) {
            builder.claim("userId", userId.toString());
        }

        return builder
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generatePasswordResetToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 900000); // 15 minutes default for password reset

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("type", "reset")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    @Override
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object val = claims.get("userId");
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getJtiFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getId();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean validateToken(String token, String expectedType) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("type", String.class);
            return expectedType != null && expectedType.equals(tokenType);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

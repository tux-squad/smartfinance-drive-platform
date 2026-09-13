package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe service for managing blacklisted JWT access and refresh tokens (revoked on logout).
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String tokenOrJti, long expirationTimeMillis) {
        if (tokenOrJti != null && !tokenOrJti.isBlank()) {
            blacklistedTokens.put(tokenOrJti, expirationTimeMillis);
        }
    }

    public boolean isBlacklisted(String tokenOrJti) {
        if (tokenOrJti == null || tokenOrJti.isBlank()) {
            return false;
        }
        Long expiration = blacklistedTokens.get(tokenOrJti);
        if (expiration == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiration) {
            blacklistedTokens.remove(tokenOrJti);
            return false;
        }
        return true;
    }
}

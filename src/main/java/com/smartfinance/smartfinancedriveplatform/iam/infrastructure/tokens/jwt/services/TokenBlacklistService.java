package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.services;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.entities.RevokedToken;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.RevokedTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database-backed thread-safe service for managing blacklisted JWT access and refresh tokens.
 */
@Service
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenBlacklistService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Transactional
    public void blacklistToken(String tokenOrJti, long expirationTimeMillis) {
        if (tokenOrJti != null && !tokenOrJti.isBlank()) {
            if (!revokedTokenRepository.existsByTokenIdentifier(tokenOrJti)) {
                revokedTokenRepository.save(new RevokedToken(tokenOrJti, expirationTimeMillis));
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String tokenOrJti) {
        if (tokenOrJti == null || tokenOrJti.isBlank()) {
            return false;
        }
        return revokedTokenRepository.findByTokenIdentifier(tokenOrJti)
                .map(revoked -> System.currentTimeMillis() <= revoked.getExpirationTimeMillis())
                .orElse(false);
    }
}

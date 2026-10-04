package com.smartfinance.smartfinancedriveplatform.iam.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.EmailVerificationSession;

import java.time.Instant;
import java.util.Optional;

/**
 * Domain repository interface for managing EmailVerificationSession persistence.
 */
public interface EmailVerificationSessionRepository {

    EmailVerificationSession save(EmailVerificationSession session);

    Optional<EmailVerificationSession> findLatestActiveSession(String email);

    Optional<EmailVerificationSession> findByVerificationToken(String token);

    void expirePendingSessions(String email);

    long countRecentSessionsByEmail(String email, Instant since);
}

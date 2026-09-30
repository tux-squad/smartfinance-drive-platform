package com.smartfinance.smartfinancedriveplatform.iam.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.CorporateVerificationSession;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for CorporateVerificationSession operations.
 */
public interface CorporateVerificationSessionRepository {

    CorporateVerificationSession save(CorporateVerificationSession session);

    Optional<CorporateVerificationSession> findById(UUID id);

    Optional<CorporateVerificationSession> findLatestActiveSession(String userId, String ruc);

    long countRecentSessionsByUserId(String userId, java.time.Instant since);

    void expirePendingSessions(String userId, String ruc);

    void deleteExpiredSessionsBefore(java.time.Instant threshold);
}

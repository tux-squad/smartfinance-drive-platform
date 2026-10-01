package com.smartfinance.smartfinancedriveplatform.iam.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for PhoneVerificationSession operations.
 */
public interface PhoneVerificationSessionRepository {

    PhoneVerificationSession save(PhoneVerificationSession session);

    Optional<PhoneVerificationSession> findById(UUID id);

    Optional<PhoneVerificationSession> findLatestActiveSession(String phoneNumber);

    long countRecentSessionsByPhoneNumber(String phoneNumber, Instant since);

    void expirePendingSessions(String phoneNumber);

    void deleteExpiredSessionsBefore(Instant threshold);
}

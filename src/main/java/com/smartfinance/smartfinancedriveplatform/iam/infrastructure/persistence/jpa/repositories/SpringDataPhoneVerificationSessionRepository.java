package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.PhoneVerificationSessionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for PhoneVerificationSessionPersistenceEntity.
 */
public interface SpringDataPhoneVerificationSessionRepository extends JpaRepository<PhoneVerificationSessionPersistenceEntity, UUID> {

    @Query("SELECT s FROM PhoneVerificationSessionPersistenceEntity s WHERE s.phoneNumber = :phoneNumber ORDER BY s.createdAt DESC LIMIT 1")
    Optional<PhoneVerificationSessionPersistenceEntity> findLatestSession(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT COUNT(s) FROM PhoneVerificationSessionPersistenceEntity s WHERE s.phoneNumber = :phoneNumber AND s.createdAt >= :since")
    long countRecentSessionsByPhoneNumber(@Param("phoneNumber") String phoneNumber, @Param("since") Instant since);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PhoneVerificationSessionPersistenceEntity s SET s.status = 'EXPIRED' WHERE s.phoneNumber = :phoneNumber AND s.status = 'PENDING'")
    void expirePendingSessions(@Param("phoneNumber") String phoneNumber);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PhoneVerificationSessionPersistenceEntity s WHERE s.expiresAt < :threshold")
    void deleteExpiredSessionsBefore(@Param("threshold") Instant threshold);
}

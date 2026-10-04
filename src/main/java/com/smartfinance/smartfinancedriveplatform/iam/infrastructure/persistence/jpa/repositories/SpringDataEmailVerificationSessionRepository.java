package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.EmailVerificationSessionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for EmailVerificationSessionPersistenceEntity.
 */
@Repository
public interface SpringDataEmailVerificationSessionRepository extends JpaRepository<EmailVerificationSessionPersistenceEntity, UUID> {

    @Query("SELECT s FROM EmailVerificationSessionPersistenceEntity s WHERE LOWER(s.email) = LOWER(:email) AND s.status = :status AND s.expiresAt > :now ORDER BY s.createdAt DESC LIMIT 1")
    Optional<EmailVerificationSessionPersistenceEntity> findFirstByEmailAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
            @Param("email") String email,
            @Param("status") EmailVerificationStatus status,
            @Param("now") Instant now
    );

    Optional<EmailVerificationSessionPersistenceEntity> findByVerificationToken(String verificationToken);

    @Modifying
    @Query("UPDATE EmailVerificationSessionPersistenceEntity s SET s.status = 'EXPIRED' WHERE LOWER(s.email) = LOWER(:email) AND s.status = 'PENDING'")
    void expirePendingSessionsByEmail(@Param("email") String email);

    @Query("SELECT COUNT(s) FROM EmailVerificationSessionPersistenceEntity s WHERE LOWER(s.email) = LOWER(:email) AND s.createdAt >= :since")
    long countRecentSessionsByEmail(@Param("email") String email, @Param("since") Instant since);
}

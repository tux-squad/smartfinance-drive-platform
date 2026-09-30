package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.CorporateVerificationSessionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for CorporateVerificationSessionPersistenceEntity.
 */
public interface SpringDataCorporateVerificationSessionRepository extends JpaRepository<CorporateVerificationSessionPersistenceEntity, UUID> {

    @Query("SELECT s FROM CorporateVerificationSessionPersistenceEntity s WHERE s.userId = :userId AND s.ruc = :ruc ORDER BY s.createdAt DESC LIMIT 1")
    Optional<CorporateVerificationSessionPersistenceEntity> findLatestSession(@Param("userId") String userId, @Param("ruc") String ruc);
}

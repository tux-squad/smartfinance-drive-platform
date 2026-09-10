package com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.scoring.infrastructure.persistence.jpa.entities.CreditScorePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for CreditScorePersistenceEntity.
 */
@Repository
public interface SpringDataCreditScoreRepository extends JpaRepository<CreditScorePersistenceEntity, UUID> {
    List<CreditScorePersistenceEntity> findByProfileId(String profileId);
}

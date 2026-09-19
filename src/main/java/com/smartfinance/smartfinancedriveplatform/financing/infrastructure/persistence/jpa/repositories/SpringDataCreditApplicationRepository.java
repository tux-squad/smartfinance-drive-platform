package com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities.CreditApplicationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for CreditApplicationPersistenceEntity.
 */
public interface SpringDataCreditApplicationRepository extends JpaRepository<CreditApplicationPersistenceEntity, UUID> {
    List<CreditApplicationPersistenceEntity> findAllByApplicantUserId(String applicantUserId);
}

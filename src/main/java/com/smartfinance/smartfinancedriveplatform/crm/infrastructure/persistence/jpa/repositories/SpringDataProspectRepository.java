package com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.entities.ProspectPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataProspectRepository extends JpaRepository<ProspectPersistenceEntity, UUID> {
    List<ProspectPersistenceEntity> findAllByDealerUserIdOrderByCreatedAtDesc(String dealerUserId);
}

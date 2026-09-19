package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.SalesAgentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataSalesAgentRepository extends JpaRepository<SalesAgentPersistenceEntity, UUID> {
    List<SalesAgentPersistenceEntity> findByDealerUserId(String dealerUserId);
}

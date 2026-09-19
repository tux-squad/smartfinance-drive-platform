package com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.entities.TestDrivePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataTestDriveRepository extends JpaRepository<TestDrivePersistenceEntity, UUID> {
    List<TestDrivePersistenceEntity> findAllByBuyerUserId(String buyerUserId);
    List<TestDrivePersistenceEntity> findAllByDealershipId(UUID dealershipId);
}

package com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.entities.DepreciationProjectionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for DepreciationProjectionPersistenceEntity.
 */
@Repository
public interface SpringDataDepreciationProjectionRepository extends JpaRepository<DepreciationProjectionPersistenceEntity, UUID> {
    List<DepreciationProjectionPersistenceEntity> findByVehicleId(String vehicleId);
}

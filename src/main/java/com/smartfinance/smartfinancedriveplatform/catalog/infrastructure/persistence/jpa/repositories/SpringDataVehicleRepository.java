package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for Vehicle persistence operations.
 */
public interface SpringDataVehicleRepository extends JpaRepository<VehiclePersistenceEntity, UUID> {
    
    /**
     * Finds all vehicle entities registered by a user.
     *
     * @param userId The user UUID.
     * @return A list of vehicle persistence entities.
     */
    List<VehiclePersistenceEntity> findAllByUserId(String userId);
}

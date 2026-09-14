package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for Vehicle persistence operations.
 */
public interface SpringDataVehicleRepository extends JpaRepository<VehiclePersistenceEntity, UUID>, JpaSpecificationExecutor<VehiclePersistenceEntity> {
    
    /**
     * Finds all vehicle entities registered by a user.
     *
     * @param userId The user UUID.
     * @return A list of vehicle persistence entities.
     */
    List<VehiclePersistenceEntity> findAllByUserId(String userId);

    /**
     * Retrieves distinct vehicle brands for lightweight fuzzy matching.
     */
    @Query("SELECT DISTINCT v.brand FROM VehiclePersistenceEntity v")
    List<String> findDistinctBrands();

    /**
     * Retrieves distinct vehicle models for lightweight fuzzy matching.
     */
    @Query("SELECT DISTINCT v.model FROM VehiclePersistenceEntity v")
    List<String> findDistinctModels();
}

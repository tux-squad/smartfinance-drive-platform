package com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities.SimulationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for SimulationPersistenceEntity.
 */
@Repository
public interface SpringDataSimulationRepository extends JpaRepository<SimulationPersistenceEntity, UUID> {
}

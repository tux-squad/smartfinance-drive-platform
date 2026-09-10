package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities.FinancialEntityPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for FinancialEntity persistence operations.
 */
public interface SpringDataFinancialEntityRepository extends JpaRepository<FinancialEntityPersistenceEntity, UUID> {

    /**
     * Finds a financial entity by its unique name.
     *
     * @param name The bank name.
     * @return An Optional containing the persistence entity if found.
     */
    Optional<FinancialEntityPersistenceEntity> findByName(String name);

    /**
     * Checks if a financial entity exists by its name.
     *
     * @param name The bank name.
     * @return true if it exists, false otherwise.
     */
    boolean existsByName(String name);
}

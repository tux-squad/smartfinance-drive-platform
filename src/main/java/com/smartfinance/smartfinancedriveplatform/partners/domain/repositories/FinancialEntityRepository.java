package com.smartfinance.smartfinancedriveplatform.partners.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for FinancialEntity operations.
 */
public interface FinancialEntityRepository {

    /**
     * Saves a FinancialEntity aggregate.
     *
     * @param financialEntity The aggregate to save.
     * @return The saved aggregate.
     */
    FinancialEntity save(FinancialEntity financialEntity);

    /**
     * Finds a FinancialEntity by its unique identifier.
     *
     * @param id The financial entity ID.
     * @return An Optional containing the aggregate if found.
     */
    Optional<FinancialEntity> findById(FinancialEntityId id);

    /**
     * Finds a FinancialEntity by its unique name.
     *
     * @param name The bank name.
     * @return An Optional containing the aggregate if found.
     */
    Optional<FinancialEntity> findByName(String name);

    /**
     * Finds all FinancialEntities.
     *
     * @return List of all financial entity aggregates.
     */
    List<FinancialEntity> findAll();

    /**
     * Checks if a FinancialEntity exists by its identifier.
     *
     * @param id The financial entity ID.
     * @return true if it exists, false otherwise.
     */
    boolean existsById(FinancialEntityId id);

    /**
     * Checks if a FinancialEntity exists by its name.
     *
     * @param name The bank name.
     * @return true if it exists, false otherwise.
     */
    boolean existsByName(String name);

    /**
     * Deletes a FinancialEntity by its identifier.
     *
     * @param id The financial entity ID.
     */
    void deleteById(FinancialEntityId id);
}

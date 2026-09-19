package com.smartfinance.smartfinancedriveplatform.partners.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Domain repository interface for Dealership aggregate operations.
 */
public interface DealershipRepository {

    /**
     * Saves a Dealership aggregate.
     *
     * @param dealership The dealership aggregate.
     * @return Saved dealership aggregate.
     */
    Dealership save(Dealership dealership);

    /**
     * Finds a Dealership by its unique ID.
     *
     * @param id The Dealership ID.
     * @return Optional containing Dealership if found.
     */
    Optional<Dealership> findById(DealershipId id);

    /**
     * Finds a Dealership by user ID string.
     *
     * @param userId The user ID string.
     * @return Optional containing Dealership if found.
     */
    Optional<Dealership> findByUserId(String userId);

    /**
     * Retrieves a page of active Dealerships for public directory listing.
     *
     * @param search   Optional search substring.
     * @param pageable Page settings.
     * @return Page of Dealership aggregates.
     */
    Page<Dealership> findAll(String search, Pageable pageable);

    /**
     * Checks if a Dealership exists by ID.
     *
     * @param id The Dealership ID.
     * @return true if exists.
     */
    boolean existsById(DealershipId id);
}

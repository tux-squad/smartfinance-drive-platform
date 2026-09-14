package com.smartfinance.smartfinancedriveplatform.catalog.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetAllVehiclesQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Vehicle operations.
 */
public interface VehicleRepository {
    
    /**
     * Saves a Vehicle aggregate.
     *
     * @param vehicle The vehicle aggregate to save.
     * @return The saved vehicle aggregate.
     */
    Vehicle save(Vehicle vehicle);

    /**
     * Finds a Vehicle by its unique identifier.
     *
     * @param id The vehicle ID.
     * @return An Optional containing the vehicle if found, or empty.
     */
    Optional<Vehicle> findById(VehicleId id);

    /**
     * Finds all Vehicles owned by a specific User.
     *
     * @param userId The user ID.
     * @return A list of vehicles.
     */
    List<Vehicle> findAllByUserId(UserId userId);

    /**
     * Finds all Vehicles matching the query search and filter parameters with pagination.
     *
     * @param query    Filter parameters.
     * @param pageable Pagination settings.
     * @return A page of vehicles.
     */
    Page<Vehicle> findAll(GetAllVehiclesQuery query, Pageable pageable);

    /**
     * Checks if a Vehicle exists by its identifier.
     *
     * @param id The vehicle ID.
     * @return true if it exists, false otherwise.
     */
    boolean existsById(VehicleId id);

    /**
     * Deletes a Vehicle by its identifier.
     *
     * @param id The vehicle ID.
     */
    void deleteById(VehicleId id);
}

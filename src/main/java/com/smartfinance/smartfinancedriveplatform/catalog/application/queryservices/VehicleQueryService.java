package com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetAllVehiclesQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehicleByIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interface declaring query operations for the Vehicle catalog application layer.
 */
public interface VehicleQueryService {

    /**
     * Handles retrieving a vehicle by its unique ID.
     *
     * @param query The query containing the vehicle ID.
     * @return An Optional containing the vehicle if found, or empty.
     */
    Optional<Vehicle> handle(GetVehicleByIdQuery query);

    /**
     * Handles retrieving all vehicles belonging to a specific user.
     *
     * @param query The query containing the user ID.
     * @return A list of vehicles.
     */
    List<Vehicle> handle(GetVehiclesByUserIdQuery query);

    /**
     * Handles retrieving all vehicles matching search/filter criteria with pagination.
     *
     * @param query    The search/filter query.
     * @param pageable Pagination settings.
     * @return A page of vehicles.
     */
    Page<Vehicle> handle(GetAllVehiclesQuery query, Pageable pageable);

    /**
     * Retrieves distinct vehicle brands.
     *
     * @return List of brand names.
     */
    List<String> getDistinctBrands();
}


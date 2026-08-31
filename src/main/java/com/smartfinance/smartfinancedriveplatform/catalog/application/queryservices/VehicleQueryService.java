package com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehicleByIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;

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
}

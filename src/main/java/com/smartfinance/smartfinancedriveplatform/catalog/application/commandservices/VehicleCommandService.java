package com.smartfinance.smartfinancedriveplatform.catalog.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.CreateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.DeleteVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.UpdateVehicleCommand;

import java.util.Optional;

/**
 * Interface declaring command operations for the Vehicle catalog application layer.
 */
public interface VehicleCommandService {
    
    /**
     * Handles the creation of a new vehicle in the catalog.
     *
     * @param command The creation command.
     * @return An Optional containing the created vehicle.
     */
    Optional<Vehicle> handle(CreateVehicleCommand command);

    /**
     * Handles updating an existing vehicle in the catalog.
     *
     * @param command The update command.
     * @return An Optional containing the updated vehicle if found, or empty.
     */
    Optional<Vehicle> handle(UpdateVehicleCommand command);

    /**
     * Handles deleting a vehicle from the catalog.
     *
     * @param command The deletion command.
     */
    void handle(DeleteVehicleCommand command);
}

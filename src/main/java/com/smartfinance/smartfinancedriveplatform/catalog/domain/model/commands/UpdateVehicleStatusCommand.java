package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;

/**
 * Command to request updating the status of an existing vehicle.
 */
public record UpdateVehicleStatusCommand(
    VehicleId vehicleId,
    String status
) {}

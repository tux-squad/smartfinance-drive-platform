package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;

/**
 * Command to request the deletion of a vehicle from the catalog.
 */
public record DeleteVehicleCommand(VehicleId vehicleId) {}

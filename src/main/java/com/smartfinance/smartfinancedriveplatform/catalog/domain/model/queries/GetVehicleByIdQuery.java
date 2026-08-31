package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;

/**
 * Query to find a single vehicle by its identifier.
 */
public record GetVehicleByIdQuery(VehicleId vehicleId) {}

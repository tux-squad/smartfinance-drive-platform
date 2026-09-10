package com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries;

/**
 * Query to retrieve depreciation projections for a specific vehicle.
 */
public record GetDepreciationProjectionsByVehicleIdQuery(String vehicleId) {}

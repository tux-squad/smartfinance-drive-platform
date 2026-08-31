package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;

/**
 * Query to find all vehicles belonging to a specific user.
 */
public record GetVehiclesByUserIdQuery(UserId userId) {}

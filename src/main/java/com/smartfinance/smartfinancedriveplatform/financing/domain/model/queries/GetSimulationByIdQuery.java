package com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;

/**
 * Query to retrieve a credit simulation by its SimulationId.
 */
public record GetSimulationByIdQuery(SimulationId simulationId) {}

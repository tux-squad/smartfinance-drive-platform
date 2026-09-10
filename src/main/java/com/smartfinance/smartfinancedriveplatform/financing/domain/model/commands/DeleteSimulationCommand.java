package com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;

/**
 * Command to delete a credit simulation by its SimulationId.
 */
public record DeleteSimulationCommand(SimulationId simulationId) {}

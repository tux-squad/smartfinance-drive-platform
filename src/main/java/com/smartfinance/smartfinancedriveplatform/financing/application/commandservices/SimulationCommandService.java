package com.smartfinance.smartfinancedriveplatform.financing.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.CreateSimulationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.DeleteSimulationCommand;

import java.util.Optional;

/**
 * Command Service interface for managing Credit Simulation mutations.
 */
public interface SimulationCommandService {
    Optional<Simulation> handle(CreateSimulationCommand command);
    void handle(DeleteSimulationCommand command);
}

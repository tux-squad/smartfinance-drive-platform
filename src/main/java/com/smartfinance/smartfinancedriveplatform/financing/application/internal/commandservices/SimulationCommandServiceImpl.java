package com.smartfinance.smartfinancedriveplatform.financing.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.SimulationCommandService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.CreateSimulationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.DeleteSimulationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.repositories.SimulationRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of SimulationCommandService application service.
 */
@Service
public class SimulationCommandServiceImpl implements SimulationCommandService {

    private final SimulationRepository simulationRepository;

    public SimulationCommandServiceImpl(SimulationRepository simulationRepository) {
        this.simulationRepository = simulationRepository;
    }

    @Override
    @Transactional
    public Optional<Simulation> handle(CreateSimulationCommand command) {
        Simulation simulation = new Simulation(
                command.title(),
                command.userId(),
                command.vehicleId(),
                command.financialEntityId(),
                command.vehiclePrice(),
                command.downPaymentPercentage(),
                command.balloonPaymentPercentage(),
                command.annualEffectiveRate(),
                command.monthlyCreditLifeInsuranceRate(),
                command.vehicleInsuranceFee(),
                command.vehicleInsuranceType(),
                command.loanTermMonths(),
                command.gracePeriodType(),
                command.gracePeriodMonths(),
                command.initialFees(),
                command.discountRate(),
                command.startDate()
        );

        Simulation savedSimulation = simulationRepository.save(simulation);
        return Optional.of(savedSimulation);
    }

    @Override
    @Transactional
    public void handle(DeleteSimulationCommand command) {
        if (!simulationRepository.existsById(command.simulationId())) {
            throw new DomainValidationException("financing.error.simulationNotFound");
        }
        simulationRepository.deleteById(command.simulationId());
    }
}

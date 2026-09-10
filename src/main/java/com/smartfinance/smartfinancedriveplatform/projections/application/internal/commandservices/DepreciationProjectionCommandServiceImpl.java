package com.smartfinance.smartfinancedriveplatform.projections.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.projections.application.commandservices.DepreciationProjectionCommandService;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates.DepreciationProjection;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.commands.CalculateDepreciationProjectionCommand;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.commands.DeleteDepreciationProjectionCommand;
import com.smartfinance.smartfinancedriveplatform.projections.domain.repositories.DepreciationProjectionRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of DepreciationProjectionCommandService application service.
 */
@Service
public class DepreciationProjectionCommandServiceImpl implements DepreciationProjectionCommandService {

    private final DepreciationProjectionRepository repository;

    public DepreciationProjectionCommandServiceImpl(DepreciationProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Optional<DepreciationProjection> handle(CalculateDepreciationProjectionCommand command) {
        DepreciationProjection projection = new DepreciationProjection(
                command.vehicleId(),
                command.simulationId(),
                command.initialVehiclePrice(),
                command.manufactureYear(),
                command.motorizationType(),
                command.balloonPaymentAmount()
        );

        DepreciationProjection savedProjection = repository.save(projection);
        return Optional.of(savedProjection);
    }

    @Override
    @Transactional
    public void handle(DeleteDepreciationProjectionCommand command) {
        if (!repository.existsById(command.projectionId())) {
            throw new DomainValidationException("projections.error.projectionNotFound");
        }
        repository.deleteById(command.projectionId());
    }
}

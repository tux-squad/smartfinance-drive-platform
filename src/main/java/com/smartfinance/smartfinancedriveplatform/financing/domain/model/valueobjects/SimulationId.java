package com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a Credit Simulation.
 */
public record SimulationId(UUID value) {
    public SimulationId {
        if (value == null) {
            throw new DomainValidationException("financing.error.simulationId.required");
        }
    }
}

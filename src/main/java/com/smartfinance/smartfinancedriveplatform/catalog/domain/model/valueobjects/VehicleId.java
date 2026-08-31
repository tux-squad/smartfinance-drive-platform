package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a Vehicle.
 */
public record VehicleId(UUID value) {
    public VehicleId {
        if (value == null) {
            throw new DomainValidationException("catalog.error.vehicleId.required");
        }
    }
}

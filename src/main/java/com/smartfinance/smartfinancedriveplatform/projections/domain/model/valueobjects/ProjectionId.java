package com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a Depreciation Projection.
 */
public record ProjectionId(UUID value) {
    public ProjectionId {
        if (value == null) {
            throw new DomainValidationException("projections.error.projectionId.required");
        }
    }
}

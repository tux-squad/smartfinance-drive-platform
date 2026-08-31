package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a User in the catalog context.
 */
public record UserId(UUID value) {
    public UserId {
        if (value == null) {
            throw new DomainValidationException("catalog.error.userId.required");
        }
    }
}

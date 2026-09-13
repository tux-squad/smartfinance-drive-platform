package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a User in the catalog context.
 */
public record UserId(String value) {
    public UserId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("catalog.error.userId.required");
        }
    }

    public UserId(UUID uuid) {
        this(uuid != null ? uuid.toString() : null);
    }

    public UserId(Long id) {
        this(id != null ? id.toString() : null);
    }
}

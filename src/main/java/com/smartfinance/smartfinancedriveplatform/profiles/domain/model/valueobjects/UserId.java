package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a User in the profiles context.
 */
public record UserId(String value) {
    public UserId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("profiles.error.userId.required");
        }
    }

    public UserId(UUID uuid) {
        this(uuid != null ? uuid.toString() : null);
    }

    public UserId(Long id) {
        this(id != null ? id.toString() : null);
    }
}

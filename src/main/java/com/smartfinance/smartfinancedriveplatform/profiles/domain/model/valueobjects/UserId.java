package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a User in the profiles context.
 */
public record UserId(UUID value) {
    public UserId {
        if (value == null) {
            throw new DomainValidationException("profiles.error.userId.required");
        }
    }
}

package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a Profile.
 */
public record ProfileId(UUID value) {
    public ProfileId {
        if (value == null) {
            throw new DomainValidationException("profiles.error.profileId.required");
        }
    }
}

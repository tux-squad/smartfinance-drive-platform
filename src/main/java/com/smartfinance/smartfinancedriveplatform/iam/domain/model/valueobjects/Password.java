package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;

/**
 * Value Object representing a user password (raw or hashed).
 */
public record Password(String password) {

    public Password {
        if (password == null || password.isBlank()) {
            throw new DomainValidationException("iam.error.password.required");
        }
    }
}

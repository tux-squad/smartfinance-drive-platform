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
        // If password is raw (not a BCrypt hash starting with $2a$, $2b$, or $2y$), validate complexity
        if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
            if (password.length() < 8) {
                throw new DomainValidationException("iam.error.password.tooShort");
            }
            if (!password.matches(".*[A-Z].*")) {
                throw new DomainValidationException("iam.error.password.missingUppercase");
            }
            if (!password.matches(".*[a-z].*")) {
                throw new DomainValidationException("iam.error.password.missingLowercase");
            }
            if (!password.matches(".*[0-9].*")) {
                throw new DomainValidationException("iam.error.password.missingDigit");
            }
        }
    }
}

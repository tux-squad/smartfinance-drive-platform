package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;

import java.util.regex.Pattern;

/**
 * Value Object representing a User's login identifier / email address.
 */
public record Username(String username) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    public Username {
        if (username == null || username.isBlank()) {
            throw new DomainValidationException("iam.error.username.required");
        }
        String trimmed = username.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new DomainValidationException("iam.error.username.invalidFormat");
        }
        username = trimmed;
    }
}

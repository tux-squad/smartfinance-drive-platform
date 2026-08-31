package com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions;

/**
 * Custom runtime exception representing business validation violations in the domain layer.
 * Should be thrown when domain invariants are violated (e.g. invalid constructor parameters).
 */
public class DomainValidationException extends RuntimeException {

    /**
     * Constructs a new DomainValidationException with the specified detail message.
     *
     * @param message the detail message key or description.
     */
    public DomainValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new DomainValidationException with the specified detail message and cause.
     *
     * @param message the detail message key or description.
     * @param cause   the cause of the exception.
     */
    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

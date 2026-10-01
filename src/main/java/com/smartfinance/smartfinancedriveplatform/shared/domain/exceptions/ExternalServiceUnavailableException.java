package com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions;

/**
 * Custom runtime exception representing an unavailability or failure in an external third-party service/gateway.
 * Maps cleanly to HTTP 503 Service Unavailable in the presentation layer.
 */
public class ExternalServiceUnavailableException extends RuntimeException {

    /**
     * Constructs a new ExternalServiceUnavailableException with the specified detail message.
     *
     * @param message the detail message key or description.
     */
    public ExternalServiceUnavailableException(String message) {
        super(message);
    }

    /**
     * Constructs a new ExternalServiceUnavailableException with the specified detail message and cause.
     *
     * @param message the detail message key or description.
     * @param cause   the cause of the exception.
     */
    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

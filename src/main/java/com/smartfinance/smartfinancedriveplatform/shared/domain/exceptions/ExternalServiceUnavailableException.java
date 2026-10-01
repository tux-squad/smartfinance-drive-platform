package com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions;

/**
 * Custom runtime exception representing an unavailability or failure in an external third-party service/gateway.
 * Maps cleanly to HTTP 503 Service Unavailable in the presentation layer.
 */
public class ExternalServiceUnavailableException extends RuntimeException {

    private final String userMessage;

    public ExternalServiceUnavailableException(String message) {
        super(message);
        this.userMessage = null;
    }

    public ExternalServiceUnavailableException(String message, String userMessage) {
        super(message);
        this.userMessage = userMessage;
    }

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
        this.userMessage = null;
    }

    public ExternalServiceUnavailableException(String message, String userMessage, Throwable cause) {
        super(message, cause);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}

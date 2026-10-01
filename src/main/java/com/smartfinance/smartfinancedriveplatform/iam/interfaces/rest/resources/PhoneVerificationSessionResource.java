package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * REST Response resource representing an initiated phone verification session.
 */
public record PhoneVerificationSessionResource(
        UUID sessionId,
        String phoneNumber,
        String status,
        Instant expiresAt,
        long expiresInSeconds,
        String message
) {
}

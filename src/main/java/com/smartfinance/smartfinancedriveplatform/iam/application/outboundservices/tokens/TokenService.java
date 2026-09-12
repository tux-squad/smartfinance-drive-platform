package com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens;

import java.util.List;

/**
 * Outbound service port interface for token generation and validation operations.
 * Decouples application services from infrastructure JWT implementations.
 */
public interface TokenService {

    /**
     * Generates a new access JWT token for a given username and list of roles.
     *
     * @param username The subject username.
     * @param roles    List of role names.
     * @return The JWT token string.
     */
    String generateToken(String username, List<String> roles);

    /**
     * Generates a new refresh token for a given username.
     *
     * @param username The subject username.
     * @return The refresh token string.
     */
    String generateRefreshToken(String username);

    /**
     * Validates a JWT token.
     *
     * @param token The token string.
     * @return true if valid, false otherwise.
     */
    boolean validateToken(String token);

    /**
     * Extracts the subject username from a valid token.
     *
     * @param token The token string.
     * @return The subject username.
     */
    String getUsernameFromToken(String token);
}

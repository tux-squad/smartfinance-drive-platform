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
     * Generates a password reset token for a given username.
     *
     * @param username The subject username.
     * @return The password reset token string.
     */
    String generatePasswordResetToken(String username);

    /**
     * Validates a JWT token.
     *
     * @param token The token string.
     * @return true if valid, false otherwise.
     */
    boolean validateToken(String token);

    /**
     * Validates a JWT token and verifies that its 'type' claim matches expectedType.
     *
     * @param token        The token string.
     * @param expectedType Expected token type ("access", "refresh", "reset").
     * @return true if valid and type matches, false otherwise.
     */
    boolean validateToken(String token, String expectedType);

    /**
     * Validates a JWT token specifically expecting type 'access'.
     */
    default boolean validateAccessToken(String token) {
        return validateToken(token, "access");
    }

    /**
     * Validates a JWT token specifically expecting type 'refresh'.
     */
    default boolean validateRefreshToken(String token) {
        return validateToken(token, "refresh");
    }

    /**
     * Validates a JWT token specifically expecting type 'reset'.
     */
    default boolean validateResetToken(String token) {
        return validateToken(token, "reset");
    }

    /**
     * Generates a new access JWT token for a given userId, username, and list of roles.
     *
     * @param userId   The user ID.
     * @param username The subject username.
     * @param roles    List of role names.
     * @return The JWT token string.
     */
    String generateToken(Long userId, String username, List<String> roles);

    /**
     * Extracts the subject username from a valid token.
     *
     * @param token The token string.
     * @return The subject username.
     */
    String getUsernameFromToken(String token);

    /**
     * Extracts the userId from a valid token if present.
     *
     * @param token The token string.
     * @return The userId as String, or null if not present.
     */
    String getUserIdFromToken(String token);

    /**
     * Extracts the JTI (JWT ID) from a valid token if present.
     *
     * @param token The token string.
     * @return The JTI string, or null if not present.
     */
    String getJtiFromToken(String token);
}

package com.smartfinance.smartfinancedriveplatform.iam.domain.services;

/**
 * Domain service interface for hashing passwords and verifying password matches.
 */
public interface HashingService {

    /**
     * Encodes a raw password string.
     *
     * @param rawPassword raw clear-text password
     * @return encoded password hash
     */
    String encode(CharSequence rawPassword);

    /**
     * Verifies if a raw password matches an encoded hash.
     *
     * @param rawPassword clear-text password candidate
     * @param encodedPassword encoded password hash
     * @return true if candidate matches hash, false otherwise
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);
}

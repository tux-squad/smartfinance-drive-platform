package com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices;

/**
 * Outbound service interface for generating and hashing cryptographically secure OTP codes.
 */
public interface OtpGeneratorService {

    /**
     * Generates a 6-digit numeric OTP string.
     */
    String generateOtp();

    /**
     * Hashes the raw OTP string using SHA-256 for secure database persistence.
     */
    String hashOtp(String otp);

    /**
     * Verifies whether a raw OTP matches the stored hash in constant time.
     */
    boolean verifyOtp(String rawOtp, String hashedOtp);
}

package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.security.services;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.OtpGeneratorService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Implementation of OtpGeneratorService using SecureRandom and SHA-256 with constant-time equality.
 */
@Service
public class OtpGeneratorServiceImpl implements OtpGeneratorService {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateOtp() {
        int code = 100_000 + secureRandom.nextInt(900_000);
        return String.valueOf(code);
    }

    @Override
    public String hashOtp(String otp) {
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("OTP code cannot be null or blank");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    public boolean verifyOtp(String rawOtp, String hashedOtp) {
        if (rawOtp == null || hashedOtp == null) {
            return false;
        }
        String computedHash = hashOtp(rawOtp);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                hashedOtp.trim().getBytes(StandardCharsets.UTF_8)
        );
    }
}

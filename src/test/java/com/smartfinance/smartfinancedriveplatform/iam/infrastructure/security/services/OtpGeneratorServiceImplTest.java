package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.security.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OtpGeneratorServiceImpl Unit Tests")
class OtpGeneratorServiceImplTest {

    private OtpGeneratorServiceImpl otpGeneratorService;

    @BeforeEach
    void setUp() {
        otpGeneratorService = new OtpGeneratorServiceImpl();
    }

    @Test
    @DisplayName("Should generate valid 6-digit numeric OTP")
    void shouldGenerateValid6DigitOtp() {
        String otp = otpGeneratorService.generateOtp();

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("^\\d{6}$"));
    }

    @Test
    @DisplayName("Should hash OTP with SHA-256")
    void shouldHashOtpWithSha256() {
        String otp = "123456";
        String hash = otpGeneratorService.hashOtp(otp);

        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 hex string has 64 chars
        assertNotEquals(otp, hash);
    }

    @Test
    @DisplayName("Should verify valid raw OTP against its hash")
    void shouldVerifyValidRawOtpAgainstHash() {
        String otp = "654321";
        String hash = otpGeneratorService.hashOtp(otp);

        assertTrue(otpGeneratorService.verifyOtp(otp, hash));
        assertFalse(otpGeneratorService.verifyOtp("000000", hash));
        assertFalse(otpGeneratorService.verifyOtp(null, hash));
        assertFalse(otpGeneratorService.verifyOtp(otp, null));
    }
}

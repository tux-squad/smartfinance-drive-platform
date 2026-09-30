package com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices;

/**
 * Outbound service interface for sending transactional emails.
 */
public interface EmailSenderService {

    /**
     * Sends a 6-digit corporate verification OTP code to the specified institutional email address.
     *
     * @param toEmail           The destination corporate email address.
     * @param recipientName     The name or username of the recipient.
     * @param entityName        The legal name of the financial entity or dealership.
     * @param otpCode           The 6-digit verification code.
     * @param expirationMinutes Minutes until the code expires.
     */
    void sendCorporateVerificationOtp(String toEmail, String recipientName, String entityName, String otpCode, int expirationMinutes);
}

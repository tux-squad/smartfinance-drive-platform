package com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices;

/**
 * Outbound port for sending phone verification codes (OTP) via external messaging gateways (e.g. WhatsApp).
 */
public interface PhoneVerificationSenderService {

    /**
     * Dispatches a verification code to the specified recipient phone number.
     *
     * @param fullPhoneNumber The normalized 11-digit Peruvian phone number (e.g. "51993913924").
     * @param code The 6-digit verification code.
     */
    void sendVerificationCode(String fullPhoneNumber, String code);
}

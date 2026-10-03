package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendPhoneVerificationCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyPhoneCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;

/**
 * Application service interface for processing phone verification write commands.
 */
public interface PhoneVerificationCommandService {

    /**
     * Generates a 6-digit OTP, creates a verification session, and dispatches it via WhatsApp.
     */
    PhoneVerificationSession handle(SendPhoneVerificationCodeCommand command);

    /**
     * Verifies the submitted OTP code against the latest active session.
     */
    PhoneVerificationResult handle(VerifyPhoneCodeCommand command);

    /**
     * Verifies a Firebase Phone Authentication ID Token and creates a verified session.
     */
    PhoneVerificationResult handle(com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyFirebasePhoneTokenCommand command);
}

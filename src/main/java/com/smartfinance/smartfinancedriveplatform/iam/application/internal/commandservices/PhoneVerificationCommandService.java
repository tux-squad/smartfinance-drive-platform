package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyFirebasePhoneTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;

/**
 * Application service interface for processing phone verification write commands.
 */
public interface PhoneVerificationCommandService {

    /**
     * Verifies a Firebase Phone Authentication ID Token and creates a verified session.
     */
    PhoneVerificationResult handle(VerifyFirebasePhoneTokenCommand command);
}

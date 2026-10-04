package com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.dto.EmailValidationResultDto;

/**
 * Outbound service interface for real-time email deliverability and MX/SMTP verification.
 */
public interface EmailValidationService {

    /**
     * Validates an email address against EmailVerify.io REST API for MX, SMTP, and mailbox existence.
     *
     * @param email The target email address to validate.
     * @return EmailValidationResultDto containing validity status and details.
     */
    EmailValidationResultDto validateEmail(String email);
}

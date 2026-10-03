package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SendEmailVerificationCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyEmailCodeCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationSent;

/**
 * Application Command Service interface for email verification flows.
 */
public interface EmailVerificationCommandService {

    EmailVerificationSent handle(SendEmailVerificationCommand command);

    EmailVerificationResult handle(VerifyEmailCodeCommand command);
}

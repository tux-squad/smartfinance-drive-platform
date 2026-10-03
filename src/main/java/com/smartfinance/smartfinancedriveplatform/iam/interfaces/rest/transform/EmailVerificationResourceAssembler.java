package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationSent;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.EmailVerificationResultResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.EmailVerificationSentResource;

/**
 * Assembler to convert domain EmailVerification value objects to REST resources.
 */
public class EmailVerificationResourceAssembler {

    public static EmailVerificationSentResource toResource(EmailVerificationSent sent) {
        if (sent == null) return null;
        return new EmailVerificationSentResource(
                sent.email(),
                sent.maskedEmail(),
                sent.sessionActive(),
                sent.expiresInSeconds(),
                sent.message()
        );
    }

    public static EmailVerificationResultResource toResource(EmailVerificationResult result) {
        if (result == null) return null;
        return new EmailVerificationResultResource(
                result.verified(),
                result.email(),
                result.status() != null ? result.status().name() : null,
                result.verifiedAt(),
                result.verificationToken(),
                result.message()
        );
    }
}

package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationInitiated;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.CorporateVerificationInitiatedResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.CorporateVerificationResultResource;

/**
 * Assembler to convert corporate verification domain value objects to REST response resources.
 */
public final class CorporateVerificationResourceAssembler {

    private CorporateVerificationResourceAssembler() {}

    public static CorporateVerificationInitiatedResource toResource(CorporateVerificationInitiated initiated) {
        return new CorporateVerificationInitiatedResource(
                initiated.sessionId(),
                initiated.sessionActive(),
                initiated.maskedEmail(),
                initiated.expiresInSeconds(),
                "Código de verificación de 6 dígitos enviado exitosamente a tu correo corporativo."
        );
    }

    public static CorporateVerificationResultResource toResource(CorporateVerificationResult result) {
        return new CorporateVerificationResultResource(
                result.verified(),
                result.entityType(),
                result.assignedRole(),
                result.profileId(),
                result.profileName(),
                result.message()
        );
    }
}

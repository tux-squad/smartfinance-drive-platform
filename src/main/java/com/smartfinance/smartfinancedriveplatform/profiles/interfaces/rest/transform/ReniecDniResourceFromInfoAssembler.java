package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ReniecDniInfo;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.ReniecDniResource;

/**
 * Assembler converting {@link ReniecDniInfo} Value Object to {@link ReniecDniResource}.
 */
public class ReniecDniResourceFromInfoAssembler {

    public static ReniecDniResource toResourceFromInfo(ReniecDniInfo info) {
        if (info == null) {
            return null;
        }
        return new ReniecDniResource(
                info.dni(),
                info.verificationDigit(),
                info.firstNames(),
                info.paternalSurname(),
                info.maternalSurname(),
                info.fullLegalName(),
                info.department(),
                info.province(),
                info.district(),
                info.address(),
                info.fullAddress(),
                info.ubigeoReniec(),
                info.ubigeoSunat(),
                info.ubigeo(),
                info.birthDate(),
                info.gender()
        );
    }
}

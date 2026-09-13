package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.SunatRucInfo;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.SunatRucResource;

/**
 * Assembler converting {@link SunatRucInfo} value object to {@link SunatRucResource}.
 */
public class SunatRucResourceFromInfoAssembler {

    public static SunatRucResource toResourceFromInfo(SunatRucInfo info) {
        return new SunatRucResource(
                info.ruc(),
                info.razonSocial(),
                info.estado(),
                info.condicion(),
                info.tipo(),
                info.ubigeo(),
                info.direccion(),
                info.ciiu(),
                info.isActiveAndHabido(),
                info.isAutomotiveCiiu()
        );
    }
}

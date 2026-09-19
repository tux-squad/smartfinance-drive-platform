package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.DealershipResource;

/**
 * Assembler to convert Dealership domain aggregate into DealershipResource response DTO.
 */
public final class DealershipResourceFromEntityAssembler {

    private DealershipResourceFromEntityAssembler() {}

    public static DealershipResource toResourceFromEntity(Dealership dealership) {
        return new DealershipResource(
            dealership.getId().value(),
            dealership.getUserId(),
            dealership.getRuc(),
            dealership.getName(),
            dealership.getAddress(),
            dealership.getPhone(),
            dealership.getEmail(),
            dealership.getWebsite(),
            dealership.getDescription(),
            dealership.getOperatingHours(),
            dealership.getRating(),
            dealership.getLogoUrl(),
            dealership.getBannerUrl(),
            dealership.isActive()
        );
    }
}

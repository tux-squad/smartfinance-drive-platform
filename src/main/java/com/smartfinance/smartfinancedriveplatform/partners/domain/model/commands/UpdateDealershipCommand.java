package com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;

/**
 * Command to request updating an existing Dealership profile details.
 */
public record UpdateDealershipCommand(
    DealershipId dealershipId,
    String ruc,
    String name,
    String address,
    String phone,
    String email,
    String website,
    String description,
    String operatingHours,
    String logoUrl,
    String bannerUrl
) {
    public UpdateDealershipCommand(DealershipId dealershipId, String ruc, String name, String address,
                                   String phone, String email, String website, String description,
                                   String operatingHours) {
        this(dealershipId, ruc, name, address, phone, email, website, description, operatingHours, null, null);
    }
}

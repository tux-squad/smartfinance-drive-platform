package com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands;

/**
 * Command to request creation of a Dealership profile.
 */
public record CreateDealershipCommand(
    String userId,
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
) {}

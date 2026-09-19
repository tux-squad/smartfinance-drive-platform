package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

import java.util.UUID;

/**
 * Resource DTO representing response payload for a Dealership.
 */
public record DealershipResource(
    UUID id,
    String userId,
    String ruc,
    String name,
    String address,
    String phone,
    String email,
    String website,
    String description,
    String operatingHours,
    Double rating,
    String logoUrl,
    String bannerUrl,
    boolean active
) {}

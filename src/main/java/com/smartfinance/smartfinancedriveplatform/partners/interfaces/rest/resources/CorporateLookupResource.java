package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources;

import java.util.List;

/**
 * REST response resource representation for B2B Corporate Verification Lookup.
 * Pre-fills profile fields from official SUNAT data and authorized domains.
 */
public record CorporateLookupResource(
        String ruc,
        String entityType,
        String targetRole,
        String suggestedName,
        String fiscalAddress,
        String ubigeo,
        List<String> allowedEmailDomains,
        String logoUrl,
        boolean eligibleForVerification
) {}

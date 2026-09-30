package com.smartfinance.smartfinancedriveplatform.partners.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CorporateLookupResource;

import java.util.Optional;

/**
 * Query service for querying official SUNAT data and pre-filling B2B corporate profile information.
 */
public interface CorporateLookupQueryService {
    Optional<CorporateLookupResource> lookupByRuc(String ruc);
}

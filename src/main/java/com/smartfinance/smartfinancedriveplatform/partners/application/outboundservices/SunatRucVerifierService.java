package com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.SunatRucInfo;

import java.util.Optional;

/**
 * Outbound service interface for querying SUNAT RUC information.
 */
public interface SunatRucVerifierService {
    Optional<SunatRucInfo> verifyRuc(String ruc);
}

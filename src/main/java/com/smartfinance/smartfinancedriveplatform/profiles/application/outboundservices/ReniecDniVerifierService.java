package com.smartfinance.smartfinancedriveplatform.profiles.application.outboundservices;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ReniecDniInfo;

import java.util.Optional;

/**
 * Outbound port for verifying and querying citizen identification data from RENIEC.
 */
public interface ReniecDniVerifierService {

    /**
     * Queries official RENIEC information for the given Peruvian DNI.
     *
     * @param dni 8-digit DNI number.
     * @return Optional containing the verified person details, or empty if not found or invalid.
     */
    Optional<ReniecDniInfo> verifyDni(String dni);
}

package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources;

import java.util.List;

/**
 * Resource representation of citizen identity data retrieved from RENIEC / Factiliza.
 */
public record ReniecDniResource(
        String dni,
        String verificationDigit,
        String firstNames,
        String paternalSurname,
        String maternalSurname,
        String fullLegalName,
        String department,
        String province,
        String district,
        String address,
        String fullAddress,
        String ubigeoReniec,
        String ubigeoSunat,
        List<String> ubigeo,
        String birthDate,
        String gender
) {}

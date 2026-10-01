package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects;

import java.util.List;

/**
 * Value Object representing verified citizen identity data from RENIEC / Factiliza.
 */
public record ReniecDniInfo(
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
        String maritalStatus,
        String photo,
        String gender
) {
    public ReniecDniInfo {
        if (dni != null) {
            dni = dni.trim();
        }
    }

    public boolean hasAddress() {
        return fullAddress != null && !fullAddress.isBlank();
    }
}

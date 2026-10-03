package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;

import java.util.regex.Pattern;

/**
 * Value Object representing a validated and normalized Peruvian mobile phone number.
 * Accepts formats such as "+51 993913924", "+51993913924", "993913924", or "51993913924"
 * and normalizes to the 11-digit format without plus "519XXXXXXXX" used in phone verification.
 */
public record PhoneNumber(String fullNumber) {

    private static final Pattern PERUVIAN_NORMALIZED_PATTERN = Pattern.compile("^519\\d{8}$");

    public PhoneNumber {
        if (fullNumber == null || fullNumber.isBlank()) {
            throw new DomainValidationException("iam.error.phoneNumber.required");
        }
        // Remove whitespace, hyphens, parentheses, and leading plus sign
        String cleaned = fullNumber.replaceAll("[\\s\\-+()]", "");

        // If user entered 9 digits starting with 9 (standard local Peruvian mobile format)
        if (cleaned.matches("^9\\d{8}$")) {
            cleaned = "51" + cleaned;
        }

        if (!PERUVIAN_NORMALIZED_PATTERN.matcher(cleaned).matches()) {
            throw new DomainValidationException("iam.error.phoneNumber.invalidFormat");
        }
        fullNumber = cleaned;
    }

    /**
     * Returns the 9-digit local Peruvian mobile number without the country code.
     */
    public String getLocalNumber() {
        return fullNumber.substring(2);
    }

    /**
     * Returns the country code with plus prefix ("+51").
     */
    public String getCountryCodeWithPlus() {
        return "+51";
    }

    /**
     * Returns the masked phone number for safe logging and UI display (e.g. "51993****24").
     */
    public String getMasked() {
        if (fullNumber != null && fullNumber.length() == 11) {
            return fullNumber.substring(0, 5) + "****" + fullNumber.substring(9);
        }
        return fullNumber;
    }
}

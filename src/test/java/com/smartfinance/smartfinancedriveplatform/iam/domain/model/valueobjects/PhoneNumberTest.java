package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PhoneNumber Value Object Unit Tests")
class PhoneNumberTest {

    @Test
    @DisplayName("Should successfully normalize user test phone number with spaces and plus (+51 993913924)")
    void shouldNormalizeUserSuppliedPhoneNumber() {
        PhoneNumber phone = new PhoneNumber("+51 993913924");

        assertEquals("51993913924", phone.fullNumber());
        assertEquals("993913924", phone.getLocalNumber());
        assertEquals("+51", phone.getCountryCodeWithPlus());
        assertEquals("51993****24", phone.getMasked());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "993913924",
            "+51993913924",
            "51993913924",
            "+51 993-913-924",
            "+51 (993) 913 924"
    })
    @DisplayName("Should normalize multiple valid Peruvian mobile phone representations into 519XXXXXXXX")
    void shouldNormalizeDifferentFormats(String input) {
        PhoneNumber phone = new PhoneNumber(input);
        assertEquals("51993913924", phone.fullNumber());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "12345",
            "51893913924", // Doesn't start with 9 for mobile
            "893913924",   // Doesn't start with 9
            "5199391392",   // Only 10 digits
            "519939139245", // 12 digits
            "invalid-phone"
    })
    @DisplayName("Should throw DomainValidationException for invalid Peruvian mobile numbers")
    void shouldThrowExceptionForInvalidPhoneNumbers(String invalid) {
        assertThrows(DomainValidationException.class, () -> new PhoneNumber(invalid));
    }

    @Test
    @DisplayName("Should throw DomainValidationException for null phone number")
    void shouldThrowExceptionForNullPhoneNumber() {
        assertThrows(DomainValidationException.class, () -> new PhoneNumber(null));
    }
}

package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

    private final UserId validUserId = new UserId(UUID.randomUUID());
    private final Money validIncome = Money.of(3500, "PEN");

    @Test
    void testValidProfileCreation() {
        Profile profile = new Profile(
            validUserId,
            "john.doe@example.com",
            "71234567",
            "John Doe",
            LocalDate.of(1990, 5, 15),
            "+51",
            "987654321",
            validIncome,
            "EMPLOYED"
        );

        assertNotNull(profile.getId());
        assertEquals(validUserId, profile.getUserId());
        assertEquals("john.doe@example.com", profile.getEmail());
        assertEquals("71234567", profile.getNationalId());
        assertEquals("John Doe", profile.getFullLegalNames());
        assertEquals(LocalDate.of(1990, 5, 15), profile.getDateOfBirth());
        assertEquals("+51", profile.getPhoneCountryCode());
        assertEquals("987654321", profile.getMobilePhone());
        assertEquals(validIncome, profile.getMonthlyIncome());
        assertEquals("EMPLOYED", profile.getEmploymentStatus());
    }

    @Test
    void testNullEmailThrowsException() {
        assertThrows(DomainValidationException.class, () -> new Profile(
            validUserId, null, "71234567", "John Doe", LocalDate.of(1990, 5, 15), "+51", "987654321", validIncome, "EMPLOYED"
        ));
    }

    @Test
    void testFutureDateOfBirthThrowsException() {
        assertThrows(DomainValidationException.class, () -> new Profile(
            validUserId, "john.doe@example.com", "71234567", "John Doe", LocalDate.now().plusDays(1), "+51", "987654321", validIncome, "EMPLOYED"
        ));
    }

    @Test
    void testUpdateDetailsSuccess() {
        Profile profile = new Profile(
            validUserId,
            "john.doe@example.com",
            "71234567",
            "John Doe",
            LocalDate.of(1990, 5, 15),
            "+51",
            "987654321",
            validIncome,
            "EMPLOYED"
        );

        Money newIncome = Money.of(4500, "PEN");

        profile.updateDetails(
            "john.updated@example.com",
            "71234568",
            "John Updated Doe",
            LocalDate.of(1990, 5, 15),
            "+51",
            "999888777",
            newIncome,
            "INDEPENDENT"
        );

        assertEquals("john.updated@example.com", profile.getEmail());
        assertEquals("71234568", profile.getNationalId());
        assertEquals("John Updated Doe", profile.getFullLegalNames());
        assertEquals(newIncome, profile.getMonthlyIncome());
        assertEquals("INDEPENDENT", profile.getEmploymentStatus());
    }
}

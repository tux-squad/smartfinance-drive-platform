package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    private final UserId validUserId = new UserId(UUID.randomUUID());
    private final FinancialEntityId validFinancialEntityId = new FinancialEntityId(UUID.randomUUID());
    private final Money validPrice = Money.of(15000, "USD");

    @Test
    void testValidVehicleCreation() {
        Vehicle vehicle = new Vehicle(
            validUserId,
            validFinancialEntityId,
            "Toyota",
            "Corolla",
            2023,
            "NEW",
            validPrice,
            "/images/corolla.jpg"
        );

        assertNotNull(vehicle.getId());
        assertEquals(validUserId, vehicle.getUserId());
        assertEquals(validFinancialEntityId, vehicle.getFinancialEntityId());
        assertEquals("Toyota", vehicle.getBrand());
        assertEquals("Corolla", vehicle.getModel());
        assertEquals(2023, vehicle.getManufactureYear());
        assertEquals("NEW", vehicle.getCondition());
        assertEquals(validPrice, vehicle.getPrice());
        assertEquals("/images/corolla.jpg", vehicle.getImagePath());
    }

    @Test
    void testInvalidBrandThrowsException() {
        assertThrows(DomainValidationException.class, () -> new Vehicle(
            validUserId, validFinancialEntityId, "", "Corolla", 2023, "NEW", validPrice, null
        ));
    }

    @Test
    void testInvalidModelThrowsException() {
        assertThrows(DomainValidationException.class, () -> new Vehicle(
            validUserId, validFinancialEntityId, "Toyota", " ", 2023, "NEW", validPrice, null
        ));
    }

    @Test
    void testInvalidManufactureYearThrowsException() {
        assertThrows(DomainValidationException.class, () -> new Vehicle(
            validUserId, validFinancialEntityId, "Toyota", "Corolla", 1899, "NEW", validPrice, null
        ));
    }

    @Test
    void testInvalidConditionThrowsException() {
        assertThrows(DomainValidationException.class, () -> new Vehicle(
            validUserId, validFinancialEntityId, "Toyota", "Corolla", 2023, "INVALID_CONDITION", validPrice, null
        ));
    }

    @Test
    void testNullPriceThrowsException() {
        assertThrows(DomainValidationException.class, () -> new Vehicle(
            validUserId, validFinancialEntityId, "Toyota", "Corolla", 2023, "NEW", null, null
        ));
    }

    @Test
    void testUpdateDetailsSuccess() {
        Vehicle vehicle = new Vehicle(
            validUserId,
            validFinancialEntityId,
            "Toyota",
            "Corolla",
            2023,
            "NEW",
            validPrice,
            null
        );

        FinancialEntityId newBankId = new FinancialEntityId(UUID.randomUUID());
        Money newPrice = Money.of(16000, "USD");

        vehicle.updateDetails(newBankId, "Hyundai", "Elantra", 2024, "used", newPrice, "/images/elantra.jpg");

        assertEquals(newBankId, vehicle.getFinancialEntityId());
        assertEquals("Hyundai", vehicle.getBrand());
        assertEquals("Elantra", vehicle.getModel());
        assertEquals(2024, vehicle.getManufactureYear());
        assertEquals("USED", vehicle.getCondition());
        assertEquals(newPrice, vehicle.getPrice());
        assertEquals("/images/elantra.jpg", vehicle.getImagePath());
    }
}

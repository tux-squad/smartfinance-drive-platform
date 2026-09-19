package com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestDriveTest {

    @Test
    void shouldCreateTestDriveWithValidFields() {
        UUID vehicleId = UUID.randomUUID();
        UUID dealershipId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        TestDrive testDrive = new TestDrive("user-123", vehicleId, dealershipId, now, "Test note");

        assertNotNull(testDrive.getId());
        assertEquals("user-123", testDrive.getBuyerUserId());
        assertEquals(vehicleId, testDrive.getVehicleId());
        assertEquals(dealershipId, testDrive.getDealershipId());
        assertEquals(now, testDrive.getScheduledDateTime());
        assertEquals("SCHEDULED", testDrive.getStatus());
        assertEquals("Test note", testDrive.getNotes());
    }

    @Test
    void shouldThrowExceptionWhenBuyerUserIdIsBlank() {
        assertThrows(DomainValidationException.class, () -> 
            new TestDrive("  ", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), "Notes")
        );
    }

    @Test
    void shouldThrowExceptionWhenVehicleIdIsNull() {
        assertThrows(DomainValidationException.class, () -> 
            new TestDrive("user-123", null, UUID.randomUUID(), LocalDateTime.now(), "Notes")
        );
    }

    @Test
    void shouldThrowExceptionWhenStatusIsInvalid() {
        TestDrive testDrive = new TestDrive("user-123", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), "Notes");
        assertThrows(DomainValidationException.class, () -> testDrive.setStatus("INVALID_STATUS"));
    }

    @Test
    void shouldUpdateStatusToCompletedOrCancelled() {
        TestDrive testDrive = new TestDrive("user-123", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), "Notes");
        testDrive.setStatus("COMPLETED");
        assertEquals("COMPLETED", testDrive.getStatus());

        testDrive.setStatus("CANCELLED");
        assertEquals("CANCELLED", testDrive.getStatus());
    }
}

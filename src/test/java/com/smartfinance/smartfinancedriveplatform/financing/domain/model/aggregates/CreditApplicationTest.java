package com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreditApplication Aggregate Unit Tests")
class CreditApplicationTest {

    private CreditApplication createSampleApplication() {
        return new CreditApplication(
                "user-123",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Money(new BigDecimal("20000.00"), "USD"),
                new Money(new BigDecimal("4000.00"), "USD"),
                36,
                new Money(new BigDecimal("5000.00"), "USD"),
                "EMPLOYED"
        );
    }

    @Test
    @DisplayName("Should create application with PENDING status")
    void shouldCreateApplicationWithPendingStatus() {
        CreditApplication app = createSampleApplication();

        assertNotNull(app.getId());
        assertEquals("user-123", app.getApplicantUserId());
        assertEquals("PENDING", app.getStatus());
        assertEquals(36, app.getTermMonths());
    }

    @Test
    @DisplayName("Should update status from PENDING to IN_REVIEW")
    void shouldUpdateStatusToInReview() {
        CreditApplication app = createSampleApplication();
        app.updateStatus("IN_REVIEW", "Reviewing documentation");

        assertEquals("IN_REVIEW", app.getStatus());
        assertEquals("Reviewing documentation", app.getNotes());
    }

    @Test
    @DisplayName("Should throw exception when attempting to change terminal status DISBURSED")
    void shouldPreventChangingTerminalDisbursedState() {
        CreditApplication app = createSampleApplication();
        app.updateStatus("PRE_APPROVED", "Pre approved");
        app.updateStatus("DISBURSED", "Disbursed funds");

        assertThrows(DomainValidationException.class, () -> 
            app.updateStatus("REJECTED", "Cannot alter terminal state")
        );
    }

    @Test
    @DisplayName("Should throw exception when reverting to PENDING from IN_REVIEW")
    void shouldPreventRevertingToPending() {
        CreditApplication app = createSampleApplication();
        app.updateStatus("IN_REVIEW", "Under review");

        assertThrows(DomainValidationException.class, () -> 
            app.updateStatus("PENDING", "Reverting back")
        );
    }

    @Test
    @DisplayName("Should throw exception when trying to DISBURSE without prior approval")
    void shouldRequireApprovalBeforeDisbursing() {
        CreditApplication app = createSampleApplication();

        assertThrows(DomainValidationException.class, () -> 
            app.updateStatus("DISBURSED", "Direct disbursement attempt")
        );
    }
}

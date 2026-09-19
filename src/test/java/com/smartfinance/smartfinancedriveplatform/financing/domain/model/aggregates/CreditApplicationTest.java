package com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreditApplication Aggregate Root Unit Tests")
class CreditApplicationTest {

    @Test
    @DisplayName("Should create credit application with PENDING status")
    void shouldCreateCreditApplication() {
        CreditApplication app = new CreditApplication(
                "user-1",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new Money(new BigDecimal("25000"), "USD"),
                new Money(new BigDecimal("5000"), "USD"),
                36,
                new Money(new BigDecimal("3500"), "USD"),
                "EMPLOYED"
        );

        assertThat(app.getId()).isNotNull();
        assertThat(app.getStatus()).isEqualTo("PENDING");
        assertThat(app.getTermMonths()).isEqualTo(36);
    }

    @Test
    @DisplayName("Should update status to PRE_APPROVED with notes")
    void shouldUpdateStatus() {
        CreditApplication app = new CreditApplication(
                "user-1",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new Money(new BigDecimal("25000"), "USD"),
                new Money(new BigDecimal("5000"), "USD"),
                36,
                new Money(new BigDecimal("3500"), "USD"),
                "EMPLOYED"
        );

        app.updateStatus("PRE_APPROVED", "Aprobado preliminarmente por BCP");

        assertThat(app.getStatus()).isEqualTo("PRE_APPROVED");
        assertThat(app.getNotes()).isEqualTo("Aprobado preliminarmente por BCP");
    }
}

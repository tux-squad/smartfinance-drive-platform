package com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Dealership Aggregate Root Unit Tests")
class DealershipTest {

    @Test
    @DisplayName("Should create valid dealership with default rating and active status")
    void shouldCreateValidDealership() {
        Dealership dealership = new Dealership(
                "user-123",
                "20100070970",
                "Derco Peru",
                "Av. Javier Prado 123",
                "987654321",
                "contacto@derco.pe",
                "https://derco.pe",
                "Concesionario oficial",
                "Lun-Vie 9-18",
                "logo.png",
                "banner.png"
        );

        assertThat(dealership.getId()).isNotNull();
        assertThat(dealership.getUserId()).isEqualTo("user-123");
        assertThat(dealership.getRuc()).isEqualTo("20100070970");
        assertThat(dealership.getName()).isEqualTo("Derco Peru");
        assertThat(dealership.getRating()).isEqualTo(5.0);
        assertThat(dealership.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when RUC is invalid")
    void shouldThrowWhenRucIsInvalid() {
        assertThatThrownBy(() -> new Dealership(
                "user-123",
                "123", // invalid
                "Derco",
                "Address",
                null, null, null, null, null, null, null
        )).isInstanceOf(DomainValidationException.class);
    }
}

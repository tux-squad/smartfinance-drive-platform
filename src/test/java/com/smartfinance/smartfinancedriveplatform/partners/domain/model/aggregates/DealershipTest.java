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

    @Test
    @DisplayName("Should update details including logo and banner URLs")
    void shouldUpdateDetailsIncludingLogoAndBanner() {
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
                "old-logo.png",
                "old-banner.png"
        );

        dealership.updateDetails(
                "20100070970",
                "Derco Peru Updated",
                "Av. Javier Prado 456",
                "912345678",
                "ventas@derco.pe",
                "https://derco-nuevo.pe",
                "Nueva descripcion",
                "Lun-Sab 9-20",
                "https://cdn.example.com/new-logo.png",
                "https://cdn.example.com/new-banner.png"
        );

        assertThat(dealership.getName()).isEqualTo("Derco Peru Updated");
        assertThat(dealership.getAddress()).isEqualTo("Av. Javier Prado 456");
        assertThat(dealership.getPhone()).isEqualTo("912345678");
        assertThat(dealership.getEmail()).isEqualTo("ventas@derco.pe");
        assertThat(dealership.getWebsite()).isEqualTo("https://derco-nuevo.pe");
        assertThat(dealership.getDescription()).isEqualTo("Nueva descripcion");
        assertThat(dealership.getOperatingHours()).isEqualTo("Lun-Sab 9-20");
        assertThat(dealership.getLogoUrl()).isEqualTo("https://cdn.example.com/new-logo.png");
        assertThat(dealership.getBannerUrl()).isEqualTo("https://cdn.example.com/new-banner.png");
    }

    @Test
    @DisplayName("Should preserve existing logo and banner when updated with null")
    void shouldPreserveExistingLogoAndBannerWhenUpdatingWithNull() {
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
                "https://cdn.example.com/initial-logo.png",
                "https://cdn.example.com/initial-banner.png"
        );

        dealership.updateDetails(
                "20100070970",
                "Derco Peru Updated",
                "Av. Javier Prado 456",
                "912345678",
                "ventas@derco.pe",
                "https://derco.pe",
                "Desc",
                "Horas",
                null,
                null
        );

        assertThat(dealership.getLogoUrl()).isEqualTo("https://cdn.example.com/initial-logo.png");
        assertThat(dealership.getBannerUrl()).isEqualTo("https://cdn.example.com/initial-banner.png");
    }

    @Test
    @DisplayName("Should sanitize blank logo and banner to null")
    void shouldSanitizeBlankLogoAndBannerToNull() {
        Dealership dealership = new Dealership(
                "user-123",
                "20100070970",
                "Derco Peru",
                "Av. Javier Prado 123",
                null, null, null, null, null, null, null
        );

        dealership.setLogoUrl("   ");
        dealership.setBannerUrl("");

        assertThat(dealership.getLogoUrl()).isNull();
        assertThat(dealership.getBannerUrl()).isNull();
    }
}

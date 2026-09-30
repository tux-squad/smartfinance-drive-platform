package com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.entities.RateBenchmark;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FinancialEntityTest {

    @Test
    void testValidFinancialEntityCreation() {
        FinancialEntity entity = new FinancialEntity("BCP");

        assertNotNull(entity.getId());
        assertEquals("BCP", entity.getName());
        assertTrue(entity.getRateBenchmarks().isEmpty());
    }

    @Test
    void testBlankNameThrowsException() {
        assertThrows(DomainValidationException.class, () -> new FinancialEntity("   "));
    }

    @Test
    void testAddRateBenchmarkSuccess() {
        FinancialEntity entity = new FinancialEntity("Interbank");

        RateBenchmark benchmark = new RateBenchmark(
            "TCEA",
            Percent.of(14.5),
            "PEN",
            "SBS Benchmark 2026",
            "https://sbs.gob.pe",
            LocalDate.of(2026, 1, 1)
        );

        entity.addRateBenchmark(benchmark);

        assertEquals(1, entity.getRateBenchmarks().size());
        assertEquals("TCEA", entity.getRateBenchmarks().get(0).getRateType());
        assertEquals("PEN", entity.getRateBenchmarks().get(0).getCurrency());
    }

    @Test
    void testCreationWithLogoAndBanner() {
        FinancialEntity entity = new FinancialEntity("BCP", "https://cdn.example.com/bcp-logo.png", "https://cdn.example.com/bcp-banner.png");

        assertNotNull(entity.getId());
        assertEquals("BCP", entity.getName());
        assertEquals("https://cdn.example.com/bcp-logo.png", entity.getLogoUrl());
        assertEquals("https://cdn.example.com/bcp-banner.png", entity.getBannerUrl());
    }

    @Test
    void testUpdateDetailsWithLogoAndBanner() {
        FinancialEntity entity = new FinancialEntity("BCP", "https://cdn.example.com/old-logo.png", "https://cdn.example.com/old-banner.png");

        entity.updateDetails("Banco de Credito del Peru", "https://cdn.example.com/new-logo.png", "https://cdn.example.com/new-banner.png");

        assertEquals("Banco de Credito del Peru", entity.getName());
        assertEquals("https://cdn.example.com/new-logo.png", entity.getLogoUrl());
        assertEquals("https://cdn.example.com/new-banner.png", entity.getBannerUrl());
    }

    @Test
    void testPreserveLogoAndBannerWhenUpdatingWithNull() {
        FinancialEntity entity = new FinancialEntity("BCP", "https://cdn.example.com/bcp-logo.png", "https://cdn.example.com/bcp-banner.png");

        entity.updateDetails("BCP Updated", null, null);

        assertEquals("BCP Updated", entity.getName());
        assertEquals("https://cdn.example.com/bcp-logo.png", entity.getLogoUrl());
        assertEquals("https://cdn.example.com/bcp-banner.png", entity.getBannerUrl());
    }

    @Test
    void testSanitizeBlankLogoAndBannerToNull() {
        FinancialEntity entity = new FinancialEntity("BCP");
        entity.setLogoUrl("   ");
        entity.setBannerUrl("");

        assertNull(entity.getLogoUrl());
        assertNull(entity.getBannerUrl());
    }
}

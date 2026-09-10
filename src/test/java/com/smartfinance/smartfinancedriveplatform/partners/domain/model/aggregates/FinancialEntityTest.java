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
}

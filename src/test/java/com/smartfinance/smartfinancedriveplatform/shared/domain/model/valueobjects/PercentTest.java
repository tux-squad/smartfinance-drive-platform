package com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PercentTest {

    @Test
    void testValidPercentCreation() {
        Percent percent = new Percent(BigDecimal.valueOf(14.5));
        assertEquals(BigDecimal.valueOf(14.500000).setScale(6), percent.value());
    }

    @Test
    void testInvalidPercentCreationThrowsException() {
        assertThrows(DomainValidationException.class, () -> new Percent(null));
        assertThrows(DomainValidationException.class, () -> new Percent(BigDecimal.valueOf(-1)));
    }

    @Test
    void testOfFactoryMethod() {
        Percent percent = Percent.of(12);
        assertEquals(BigDecimal.valueOf(12.000000).setScale(6), percent.value());
    }

    @Test
    void testFromDecimalFactoryMethod() {
        Percent percent = Percent.fromDecimal(BigDecimal.valueOf(0.145));
        assertEquals(BigDecimal.valueOf(14.500000).setScale(6), percent.value());
    }

    @Test
    void testToDecimalConversion() {
        Percent percent = Percent.of(14.5);
        BigDecimal decimal = percent.toDecimal();
        // 14.5% = 0.145
        assertEquals(BigDecimal.valueOf(0.145).setScale(8), decimal);
    }

    @Test
    void testToStringFormatting() {
        Percent percent = Percent.of(14.5);
        assertEquals("14.5%", percent.toString());

        Percent percentWhole = Percent.of(12);
        assertEquals("12%", percentWhole.toString());
    }
}

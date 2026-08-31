package com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void testValidMoneyCreation() {
        Money money = new Money(BigDecimal.valueOf(100.5), "USD");
        assertEquals(BigDecimal.valueOf(100.50).setScale(2), money.amount());
        assertEquals("USD", money.currency());
    }

    @Test
    void testMoneyCreationWithDefaultCurrency() {
        Money money = new Money(BigDecimal.valueOf(50));
        assertEquals(BigDecimal.valueOf(50.00).setScale(2), money.amount());
        assertEquals("PEN", money.currency());
    }

    @Test
    void testInvalidMoneyCreationThrowsException() {
        assertThrows(DomainValidationException.class, () -> new Money(null, "USD"));
        assertThrows(DomainValidationException.class, () -> new Money(BigDecimal.valueOf(100), ""));
        assertThrows(DomainValidationException.class, () -> new Money(BigDecimal.valueOf(-10), "USD"));
    }

    @Test
    void testPlusOperationSuccess() {
        Money m1 = Money.of(100.50, "PEN");
        Money m2 = Money.of(50.25, "PEN");
        Money result = m1.plus(m2);

        assertEquals(BigDecimal.valueOf(150.75).setScale(2), result.amount());
        assertEquals("PEN", result.currency());
    }

    @Test
    void testPlusOperationWithMismatchingCurrenciesThrowsException() {
        Money m1 = Money.of(100, "PEN");
        Money m2 = Money.of(50, "USD");

        assertThrows(DomainValidationException.class, () -> m1.plus(m2));
    }

    @Test
    void testMinusOperationSuccess() {
        Money m1 = Money.of(100.50, "PEN");
        Money m2 = Money.of(50.25, "PEN");
        Money result = m1.minus(m2);

        assertEquals(BigDecimal.valueOf(50.25).setScale(2), result.amount());
        assertEquals("PEN", result.currency());
    }

    @Test
    void testMinusOperationWithMismatchingCurrenciesThrowsException() {
        Money m1 = Money.of(100, "PEN");
        Money m2 = Money.of(50, "USD");

        assertThrows(DomainValidationException.class, () -> m1.minus(m2));
    }

    @Test
    void testMultiplyIntOperation() {
        Money money = Money.of(20.50, "USD");
        Money result = money.multiply(3);

        assertEquals(BigDecimal.valueOf(61.50).setScale(2), result.amount());
        assertEquals("USD", result.currency());
    }

    @Test
    void testMultiplyDecimalOperation() {
        Money money = Money.of(20.50, "USD");
        Money result = money.multiply(BigDecimal.valueOf(1.5));

        assertEquals(BigDecimal.valueOf(30.75).setScale(2), result.amount());
        assertEquals("USD", result.currency());
    }

    @Test
    void testComparisonOperations() {
        Money m1 = Money.of(100, "PEN");
        Money m2 = Money.of(50, "PEN");

        assertTrue(m1.isGreaterThan(m2));
        assertFalse(m2.isGreaterThan(m1));

        assertTrue(m2.isLessThan(m1));
        assertFalse(m1.isLessThan(m2));
    }
}

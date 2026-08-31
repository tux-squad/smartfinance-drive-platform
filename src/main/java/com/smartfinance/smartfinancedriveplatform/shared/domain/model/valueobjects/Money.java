package com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object representing a monetary amount with currency.
 * Encapsulates a BigDecimal to ensure precision and prevent rounding errors.
 *
 * @param amount   The monetary value. Must be non-null and non-negative.
 * @param currency The ISO 4217 currency code (e.g., "PEN", "USD"). Must be non-null.
 */
public record Money(BigDecimal amount, String currency) {

    public static final String DEFAULT_CURRENCY = "PEN";

    /**
     * Compact constructor for validation and normalization.
     */
    public Money {
        if (amount == null) {
            throw new DomainValidationException("shared.error.money.amount.required");
        }
        if (currency == null || currency.isBlank()) {
            throw new DomainValidationException("shared.error.money.currency.required");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("shared.error.money.amount.negative");
        }
        // Normalize to 2 decimal places with HALF_UP rounding
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        currency = currency.trim().toUpperCase();
    }

    /**
     * Secondary constructor for default currency.
     */
    public Money(BigDecimal amount) {
        this(amount, DEFAULT_CURRENCY);
    }

    /**
     * Factory method to create Money from double value.
     */
    public static Money of(double amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Factory method to create Money from double with default currency.
     */
    public static Money of(double amount) {
        return of(amount, DEFAULT_CURRENCY);
    }

    /**
     * Returns a Money instance representing zero in the specified currency.
     */
    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * Returns a Money instance representing zero in the default currency.
     */
    public static Money zero() {
        return zero(DEFAULT_CURRENCY);
    }

    /**
     * Adds another Money amount. Currencies must match.
     */
    public Money plus(Money other) {
        if (other == null) return this;
        checkCurrenciesMatch(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtracts another Money amount. Currencies must match.
     */
    public Money minus(Money other) {
        if (other == null) return this;
        checkCurrenciesMatch(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Multiplies the Money amount by an integer quantity.
     */
    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    /**
     * Multiplies the Money amount by a decimal factor.
     */
    public Money multiply(BigDecimal factor) {
        if (factor == null) return this;
        return new Money(this.amount.multiply(factor), this.currency);
    }

    /**
     * Checks if this amount is greater than another Money amount.
     */
    public boolean isGreaterThan(Money other) {
        if (other == null) return false;
        checkCurrenciesMatch(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks if this amount is less than another Money amount.
     */
    public boolean isLessThan(Money other) {
        if (other == null) return false;
        checkCurrenciesMatch(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    private void checkCurrenciesMatch(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException(String.format(
                    "shared.error.money.currency.mismatch: Cannot operate on %s and %s",
                    this.currency, other.currency));
        }
    }
}

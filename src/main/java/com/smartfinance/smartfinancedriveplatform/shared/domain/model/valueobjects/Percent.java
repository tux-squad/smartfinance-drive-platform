package com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object representing a percentage value (e.g., interest rate, insurance rate).
 * Encapsulates a BigDecimal to prevent rounding issues during calculations.
 *
 * @param value The percentage value (e.g., 14.5 for 14.5%). Must be non-null and non-negative.
 */
public record Percent(BigDecimal value) {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /**
     * Compact constructor for validation and normalization.
     */
    public Percent {
        if (value == null) {
            throw new IllegalArgumentException("shared.error.percent.value.required");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("shared.error.percent.value.negative");
        }
        // Normalize percentage to 6 decimal places for interest rate precision
        value = value.setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * Factory method to create Percent from double.
     */
    public static Percent of(double percentageValue) {
        return new Percent(BigDecimal.valueOf(percentageValue));
    }

    /**
     * Factory method to create Percent from a decimal fraction (e.g., 0.145 -> 14.5%).
     */
    public static Percent fromDecimal(BigDecimal decimalFraction) {
        if (decimalFraction == null) {
            throw new IllegalArgumentException("shared.error.percent.decimal.required");
        }
        return new Percent(decimalFraction.multiply(HUNDRED));
    }

    /**
     * Converts the percentage to its decimal fraction equivalent (e.g., 14.5% -> 0.145000).
     */
    public BigDecimal toDecimal() {
        return this.value.divide(HUNDRED, 8, RoundingMode.HALF_UP);
    }

    /**
     * Returns a string representation of the percentage, e.g., "14.500000%".
     */
    @Override
    public String toString() {
        return String.format("%s%%", value.stripTrailingZeros().toPlainString());
    }
}

package com.smartfinance.smartfinancedriveplatform.projections.domain.services;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.MotorizationType;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.RecommendedAction;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain Service responsible for calculating vehicle depreciation curves
 * and generating smart financial trade-in/return recommendations.
 */
public class DepreciationCalculator {

    public record CalculationOutput(
            Money projectedValue2Years,
            Money projectedValue3Years,
            Money projectedValue5Years,
            Percent annualDepreciationRate,
            RecommendedAction recommendedAction,
            String advisoryNotes
    ) {}

    public CalculationOutput calculateProjection(
            Money initialVehiclePrice,
            MotorizationType motorizationType,
            Money balloonPaymentAmount
    ) {
        if (initialVehiclePrice == null || initialVehiclePrice.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("projections.error.initialVehiclePrice.invalid");
        }

        String currency = initialVehiclePrice.currency();
        MotorizationType type = motorizationType != null ? motorizationType : MotorizationType.COMBUSTION;

        double year1Rate = (type == MotorizationType.ECOLOGICO) ? 0.12 : 0.18;
        double laterRate = (type == MotorizationType.ECOLOGICO) ? 0.07 : 0.10;

        BigDecimal price = initialVehiclePrice.amount();

        BigDecimal v1 = price.multiply(BigDecimal.valueOf(1.0 - year1Rate)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal v2 = v1.multiply(BigDecimal.valueOf(1.0 - laterRate)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal v3 = v2.multiply(BigDecimal.valueOf(1.0 - laterRate)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal v4 = v3.multiply(BigDecimal.valueOf(1.0 - laterRate)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal v5 = v4.multiply(BigDecimal.valueOf(1.0 - laterRate)).setScale(2, RoundingMode.HALF_UP);

        Money val2 = new Money(v2, currency);
        Money val3 = new Money(v3, currency);
        Money val5 = new Money(v5, currency);

        Percent annualDepreciationRate = Percent.of(laterRate * 100.0);

        RecommendedAction action;
        String notes;

        if (balloonPaymentAmount != null && balloonPaymentAmount.amount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal balloon = balloonPaymentAmount.amount();
            BigDecimal tradeInThreshold = balloon.multiply(BigDecimal.valueOf(1.15));

            if (v3.compareTo(tradeInThreshold) > 0) {
                action = RecommendedAction.TRADE_IN;
                notes = String.format("Vehicle retains strong market equity (%s %s vs %s %s balloon). Recommended to trade-in for a new model.",
                        v3.toPlainString(), currency, balloon.toPlainString(), currency);
            } else if (v3.compareTo(balloon) >= 0) {
                action = RecommendedAction.KEEP_AND_PAY;
                notes = String.format("Vehicle market value (%s %s) covers balloon payment (%s %s). Recommended to pay balloon and keep car.",
                        v3.toPlainString(), currency, balloon.toPlainString(), currency);
            } else {
                action = RecommendedAction.RETURN_VEHICLE;
                notes = String.format("Market value (%s %s) fell below balloon payment (%s %s). Recommended to return vehicle to avoid negative equity.",
                        v3.toPlainString(), currency, balloon.toPlainString(), currency);
            }
        } else {
            action = RecommendedAction.KEEP_AND_PAY;
            notes = String.format("Depreciation projection calculated for %s motorization.", type.name());
        }

        return new CalculationOutput(val2, val3, val5, annualDepreciationRate, action, notes);
    }
}

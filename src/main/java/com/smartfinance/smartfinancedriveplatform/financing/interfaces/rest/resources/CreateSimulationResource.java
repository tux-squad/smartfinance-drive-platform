package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resource DTO representing the request payload for creating a credit simulation.
 */
public record CreateSimulationResource(
        String title,
        String userId,
        String vehicleId,
        String financialEntityId,
        BigDecimal vehiclePriceAmount,
        String currency,
        BigDecimal downPaymentPercentage,
        BigDecimal balloonPaymentPercentage,
        BigDecimal annualEffectiveRate,
        BigDecimal monthlyCreditLifeInsuranceRate,
        BigDecimal vehicleInsuranceFeeAmount,
        String vehicleInsuranceType,
        int loanTermMonths,
        String gracePeriodType,
        int gracePeriodMonths,
        BigDecimal initialFeesAmount,
        BigDecimal discountRate,
        LocalDate startDate
) {}

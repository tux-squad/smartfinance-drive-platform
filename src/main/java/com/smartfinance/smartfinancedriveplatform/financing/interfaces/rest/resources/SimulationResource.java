package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Resource DTO representing the response payload for a Credit Simulation.
 */
public record SimulationResource(
        UUID id,
        String title,
        String userId,
        String vehicleId,
        String financialEntityId,
        String currency,
        BigDecimal vehiclePriceAmount,
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
        LocalDate startDate,
        FinancialMetricsResource metrics,
        List<PaymentPeriodResource> paymentSchedule
) {}

package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resource DTO representing an individual payment period in a credit simulation response.
 */
public record PaymentPeriodResource(
        UUID id,
        int periodNumber,
        LocalDate dueDate,
        int daysInPeriod,
        String currency,
        BigDecimal initialBalanceAmount,
        BigDecimal interestPaymentAmount,
        BigDecimal principalAmortizationAmount,
        BigDecimal creditLifeInsuranceAmount,
        BigDecimal vehicleInsuranceAmount,
        BigDecimal totalInstallmentAmount,
        BigDecimal finalBalanceAmount,
        String graceType
) {}

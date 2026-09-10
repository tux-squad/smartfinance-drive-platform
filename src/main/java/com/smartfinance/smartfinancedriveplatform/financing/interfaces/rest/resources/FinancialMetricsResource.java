package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Resource DTO representing computed financial metrics (TIR, TCEA, VAN, etc.) in a response.
 */
public record FinancialMetricsResource(
        BigDecimal financedAmount,
        BigDecimal downPaymentAmount,
        BigDecimal balloonPaymentAmount,
        BigDecimal tcea,
        BigDecimal tir,
        BigDecimal van,
        BigDecimal totalInterest,
        BigDecimal totalAmount
) {}

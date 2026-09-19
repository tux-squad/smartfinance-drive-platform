package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Resource DTO representing response payload for a CreditApplication.
 */
public record CreditApplicationResource(
    UUID id,
    String applicantUserId,
    UUID vehicleId,
    UUID financialEntityId,
    UUID simulationId,
    BigDecimal requestedAmount,
    BigDecimal downPayment,
    int termMonths,
    BigDecimal monthlyIncome,
    String currency,
    String employmentStatus,
    String status,
    String notes,
    Instant createdAt
) {
    public CreditApplicationResource(UUID id, String applicantUserId, UUID vehicleId, UUID financialEntityId, UUID simulationId, BigDecimal requestedAmount, BigDecimal downPayment, int termMonths, BigDecimal monthlyIncome, String currency, String employmentStatus, String status, String notes) {
        this(id, applicantUserId, vehicleId, financialEntityId, simulationId, requestedAmount, downPayment, termMonths, monthlyIncome, currency, employmentStatus, status, notes, Instant.now());
    }
}


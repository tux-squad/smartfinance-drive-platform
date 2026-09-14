package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceResource(
        Long id,
        Long subscriptionId,
        String userId,
        BigDecimal amount,
        String currency,
        InvoiceStatus status,
        LocalDateTime issuedAt,
        LocalDateTime dueDate,
        LocalDateTime paidAt
) {}

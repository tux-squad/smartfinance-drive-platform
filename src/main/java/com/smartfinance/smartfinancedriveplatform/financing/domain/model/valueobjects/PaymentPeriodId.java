package com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a Payment Period.
 */
public record PaymentPeriodId(UUID value) {
    public PaymentPeriodId {
        if (value == null) {
            throw new DomainValidationException("financing.error.paymentPeriodId.required");
        }
    }
}

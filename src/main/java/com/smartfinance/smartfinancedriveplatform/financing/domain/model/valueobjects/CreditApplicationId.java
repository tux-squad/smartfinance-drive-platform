package com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing unique identifier of a CreditApplication.
 */
public record CreditApplicationId(UUID value) {
    public CreditApplicationId {
        if (value == null) {
            throw new DomainValidationException("financing.error.creditApplicationId.required");
        }
    }
}

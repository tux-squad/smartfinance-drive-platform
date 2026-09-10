package com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a Financial Entity.
 */
public record FinancialEntityId(UUID value) {
    public FinancialEntityId {
        if (value == null) {
            throw new DomainValidationException("partners.error.financialEntityId.required");
        }
    }
}

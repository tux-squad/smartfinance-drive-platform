package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a Financial Entity in the catalog context.
 */
public record FinancialEntityId(UUID value) {
    public FinancialEntityId {
        if (value == null) {
            throw new DomainValidationException("catalog.error.financialEntityId.required");
        }
    }
}

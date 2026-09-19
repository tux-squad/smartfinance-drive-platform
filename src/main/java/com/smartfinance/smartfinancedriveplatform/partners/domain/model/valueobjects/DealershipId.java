package com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a Dealership.
 */
public record DealershipId(UUID value) {
    public DealershipId {
        if (value == null) {
            throw new DomainValidationException("partners.error.dealershipId.required");
        }
    }
}

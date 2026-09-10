package com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a Rate Benchmark.
 */
public record RateBenchmarkId(UUID value) {
    public RateBenchmarkId {
        if (value == null) {
            throw new DomainValidationException("partners.error.rateBenchmarkId.required");
        }
    }
}

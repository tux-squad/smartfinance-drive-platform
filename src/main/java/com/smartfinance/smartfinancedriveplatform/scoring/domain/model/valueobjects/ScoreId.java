package com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

/**
 * Value Object representing the unique identifier of a Credit Score evaluation.
 */
public record ScoreId(UUID value) {
    public ScoreId {
        if (value == null) {
            throw new DomainValidationException("scoring.error.scoreId.required");
        }
    }
}

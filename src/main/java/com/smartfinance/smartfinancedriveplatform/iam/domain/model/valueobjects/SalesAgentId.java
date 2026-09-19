package com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

public record SalesAgentId(UUID value) {
    public SalesAgentId {
        if (value == null) {
            throw new DomainValidationException("iam.error.salesAgentId.required");
        }
    }
}

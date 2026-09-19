package com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

public record ProspectId(UUID value) {
    public ProspectId {
        if (value == null) {
            throw new DomainValidationException("crm.error.prospectId.required");
        }
    }
}

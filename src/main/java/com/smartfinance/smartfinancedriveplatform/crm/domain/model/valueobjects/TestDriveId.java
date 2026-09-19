package com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

public record TestDriveId(UUID value) {
    public TestDriveId {
        if (value == null) {
            throw new DomainValidationException("crm.error.testDriveId.required");
        }
    }
}

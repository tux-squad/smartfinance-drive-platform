package com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

public record MessageId(UUID value) {
    public MessageId {
        if (value == null) {
            throw new DomainValidationException("messaging.error.messageId.required");
        }
    }
}

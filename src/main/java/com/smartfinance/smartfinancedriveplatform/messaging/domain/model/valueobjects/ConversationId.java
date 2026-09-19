package com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects;

import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import java.util.UUID;

public record ConversationId(UUID value) {
    public ConversationId {
        if (value == null) {
            throw new DomainValidationException("messaging.error.conversationId.required");
        }
    }
}

package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.entities.Message;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources.MessageResource;

public final class MessageResourceFromEntityAssembler {

    private MessageResourceFromEntityAssembler() {}

    public static MessageResource toResourceFromEntity(Message domain) {
        return new MessageResource(
            domain.getId().value(),
            domain.getConversationId(),
            domain.getSenderUserId(),
            domain.getContent(),
            domain.getAttachmentUrl(),
            domain.getSentAt(),
            domain.isRead()
        );
    }
}

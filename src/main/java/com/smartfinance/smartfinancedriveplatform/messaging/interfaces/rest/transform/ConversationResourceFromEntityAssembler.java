package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates.Conversation;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources.ConversationResource;

public final class ConversationResourceFromEntityAssembler {

    private ConversationResourceFromEntityAssembler() {}

    public static ConversationResource toResourceFromEntity(Conversation domain) {
        return new ConversationResource(
            domain.getId().value(),
            domain.getBuyerUserId(),
            domain.getDealerUserId(),
            domain.getVehicleId(),
            domain.getLastMessageContent(),
            domain.getLastMessageTimestamp(),
            domain.getUnreadBuyerCount(),
            domain.getUnreadDealerCount(),
            domain.isActive()
        );
    }
}

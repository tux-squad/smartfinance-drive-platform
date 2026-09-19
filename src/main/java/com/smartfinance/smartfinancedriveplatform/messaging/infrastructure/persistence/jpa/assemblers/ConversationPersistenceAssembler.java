package com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates.Conversation;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;
import com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.entities.ConversationPersistenceEntity;

public final class ConversationPersistenceAssembler {

    private ConversationPersistenceAssembler() {}

    public static ConversationPersistenceEntity toEntity(Conversation domain, ConversationPersistenceEntity entity) {
        if (entity == null) {
            entity = new ConversationPersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setBuyerUserId(domain.getBuyerUserId());
        entity.setDealerUserId(domain.getDealerUserId());
        entity.setVehicleId(domain.getVehicleId());
        entity.setLastMessageContent(domain.getLastMessageContent());
        entity.setLastMessageTimestamp(domain.getLastMessageTimestamp());
        entity.setUnreadBuyerCount(domain.getUnreadBuyerCount());
        entity.setUnreadDealerCount(domain.getUnreadDealerCount());
        entity.setActive(domain.isActive());
        return entity;
    }

    public static Conversation toDomain(ConversationPersistenceEntity entity) {
        return new Conversation(
            new ConversationId(entity.getId()),
            entity.getBuyerUserId(),
            entity.getDealerUserId(),
            entity.getVehicleId(),
            entity.getLastMessageContent(),
            entity.getLastMessageTimestamp(),
            entity.getUnreadBuyerCount(),
            entity.getUnreadDealerCount(),
            entity.isActive()
        );
    }
}

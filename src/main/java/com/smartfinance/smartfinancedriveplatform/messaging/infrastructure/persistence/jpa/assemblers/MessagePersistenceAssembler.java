package com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.entities.Message;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.MessageId;
import com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.entities.MessagePersistenceEntity;

public final class MessagePersistenceAssembler {

    private MessagePersistenceAssembler() {}

    public static MessagePersistenceEntity toEntity(Message domain, MessagePersistenceEntity entity) {
        if (entity == null) {
            entity = new MessagePersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setConversationId(domain.getConversationId());
        entity.setSenderUserId(domain.getSenderUserId());
        entity.setContent(domain.getContent());
        entity.setAttachmentUrl(domain.getAttachmentUrl());
        entity.setSentAt(domain.getSentAt());
        entity.setRead(domain.isRead());
        return entity;
    }

    public static Message toDomain(MessagePersistenceEntity entity) {
        return new Message(
            new MessageId(entity.getId()),
            entity.getConversationId(),
            entity.getSenderUserId(),
            entity.getContent(),
            entity.getAttachmentUrl(),
            entity.getSentAt(),
            entity.isRead()
        );
    }
}

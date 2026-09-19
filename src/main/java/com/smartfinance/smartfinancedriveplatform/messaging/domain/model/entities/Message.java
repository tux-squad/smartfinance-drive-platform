package com.smartfinance.smartfinancedriveplatform.messaging.domain.model.entities;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.MessageId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a chat message.
 */
@Getter
public class Message {

    private final MessageId id;
    private final UUID conversationId;
    private final String senderUserId;
    private final String content;
    private final String attachmentUrl;
    private final Instant sentAt;
    private boolean read;

    public Message(MessageId id, UUID conversationId, String senderUserId, String content, String attachmentUrl, Instant sentAt, boolean read) {
        if (id == null) throw new DomainValidationException("messaging.error.messageId.required");
        if (conversationId == null) throw new DomainValidationException("messaging.error.conversationId.required");
        if (senderUserId == null || senderUserId.isBlank()) throw new DomainValidationException("messaging.error.senderUserId.required");
        if ((content == null || content.isBlank()) && (attachmentUrl == null || attachmentUrl.isBlank())) {
            throw new DomainValidationException("messaging.error.messageContent.required");
        }
        this.id = id;
        this.conversationId = conversationId;
        this.senderUserId = senderUserId.trim();
        this.content = content != null ? content.trim() : "";
        this.attachmentUrl = attachmentUrl != null ? attachmentUrl.trim() : null;
        this.sentAt = sentAt != null ? sentAt : Instant.now();
        this.read = read;
    }

    public Message(UUID conversationId, String senderUserId, String content, String attachmentUrl) {
        this(new MessageId(UUID.randomUUID()), conversationId, senderUserId, content, attachmentUrl, Instant.now(), false);
    }

    public void markAsRead() {
        this.read = true;
    }
}

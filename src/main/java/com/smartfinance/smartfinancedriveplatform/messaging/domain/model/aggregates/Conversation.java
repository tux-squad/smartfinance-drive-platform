package com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Conversation Aggregate Root.
 * Represents a chat session between a buyer user and a dealer user.
 */
@Getter
public class Conversation extends AbstractDomainAggregateRoot<Conversation> {

    private final ConversationId id;
    private String buyerUserId;
    private String dealerUserId;
    private UUID vehicleId;
    private String lastMessageContent;
    private Instant lastMessageTimestamp;
    private int unreadBuyerCount;
    private int unreadDealerCount;
    private boolean active;

    public Conversation(ConversationId id, String buyerUserId, String dealerUserId, UUID vehicleId,
                        String lastMessageContent, Instant lastMessageTimestamp,
                        int unreadBuyerCount, int unreadDealerCount, boolean active) {
        this.id = id;
        setBuyerUserId(buyerUserId);
        setDealerUserId(dealerUserId);
        this.vehicleId = vehicleId;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageTimestamp = lastMessageTimestamp != null ? lastMessageTimestamp : Instant.now();
        this.unreadBuyerCount = Math.max(0, unreadBuyerCount);
        this.unreadDealerCount = Math.max(0, unreadDealerCount);
        this.active = active;
    }

    public Conversation(String buyerUserId, String dealerUserId, UUID vehicleId) {
        this(new ConversationId(UUID.randomUUID()), buyerUserId, dealerUserId, vehicleId, null, Instant.now(), 0, 0, true);
    }

    public void setBuyerUserId(String buyerUserId) {
        if (buyerUserId == null || buyerUserId.isBlank()) {
            throw new DomainValidationException("messaging.error.buyerUserId.required");
        }
        this.buyerUserId = buyerUserId.trim();
    }

    public void setDealerUserId(String dealerUserId) {
        if (dealerUserId == null || dealerUserId.isBlank()) {
            throw new DomainValidationException("messaging.error.dealerUserId.required");
        }
        this.dealerUserId = dealerUserId.trim();
    }

    public void updateLastMessage(String messageContent, String senderUserId, Instant timestamp) {
        this.lastMessageContent = messageContent;
        this.lastMessageTimestamp = timestamp != null ? timestamp : Instant.now();
        if (senderUserId.equals(buyerUserId)) {
            this.unreadDealerCount++;
        } else {
            this.unreadBuyerCount++;
        }
    }

    public void markAsReadForUser(String userId) {
        if (userId.equals(buyerUserId)) {
            this.unreadBuyerCount = 0;
        } else if (userId.equals(dealerUserId)) {
            this.unreadDealerCount = 0;
        }
    }
}

package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record ConversationResource(
    UUID id,
    String buyerUserId,
    String dealerUserId,
    UUID vehicleId,
    String lastMessageContent,
    Instant lastMessageTimestamp,
    int unreadBuyerCount,
    int unreadDealerCount,
    boolean active,
    Instant createdAt
) {
    public ConversationResource(UUID id, String buyerUserId, String dealerUserId, UUID vehicleId, String lastMessageContent, Instant lastMessageTimestamp, int unreadBuyerCount, int unreadDealerCount, boolean active) {
        this(id, buyerUserId, dealerUserId, vehicleId, lastMessageContent, lastMessageTimestamp, unreadBuyerCount, unreadDealerCount, active, lastMessageTimestamp != null ? lastMessageTimestamp : Instant.now());
    }
}


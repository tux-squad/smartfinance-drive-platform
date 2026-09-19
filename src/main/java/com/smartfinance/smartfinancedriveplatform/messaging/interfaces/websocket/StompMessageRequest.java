package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.websocket;

import java.util.UUID;

public record StompMessageRequest(
        UUID conversationId,
        String senderUserId,
        String content
) {
}

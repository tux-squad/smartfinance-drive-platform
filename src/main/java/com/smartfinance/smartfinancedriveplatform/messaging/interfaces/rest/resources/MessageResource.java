package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record MessageResource(
    UUID id,
    UUID conversationId,
    String senderUserId,
    String content,
    String attachmentUrl,
    Instant sentAt,
    boolean read
) {}

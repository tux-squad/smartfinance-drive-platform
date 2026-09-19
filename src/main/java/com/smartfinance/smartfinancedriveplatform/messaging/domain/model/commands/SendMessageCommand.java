package com.smartfinance.smartfinancedriveplatform.messaging.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;

public record SendMessageCommand(
    ConversationId conversationId,
    String senderUserId,
    String content,
    String attachmentUrl
) {}

package com.smartfinance.smartfinancedriveplatform.messaging.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;

public record GetMessagesByConversationIdQuery(ConversationId conversationId) {}

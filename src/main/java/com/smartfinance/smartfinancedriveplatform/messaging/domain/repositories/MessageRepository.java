package com.smartfinance.smartfinancedriveplatform.messaging.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.entities.Message;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;

import java.util.List;

public interface MessageRepository {
    Message save(Message message);
    List<Message> findAllByConversationIdOrderBySentAtAsc(ConversationId conversationId);
    void markAllAsReadForConversation(ConversationId conversationId, String excludeSenderUserId);
}

package com.smartfinance.smartfinancedriveplatform.messaging.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates.Conversation;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.entities.Message;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.queries.GetConversationsForUserQuery;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.queries.GetMessagesByConversationIdQuery;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;

import java.util.List;
import java.util.Optional;

public interface MessagingQueryService {

    List<Conversation> handle(GetConversationsForUserQuery query);

    List<Message> handle(GetMessagesByConversationIdQuery query);

    Optional<Conversation> getConversationById(ConversationId conversationId);
}

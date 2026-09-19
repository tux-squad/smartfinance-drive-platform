package com.smartfinance.smartfinancedriveplatform.messaging.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates.Conversation;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.commands.CreateConversationCommand;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.commands.SendMessageCommand;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.entities.Message;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;

import java.util.Optional;

public interface MessagingCommandService {

    Optional<Conversation> handle(CreateConversationCommand command);

    Optional<Message> handle(SendMessageCommand command);

    void markConversationAsRead(ConversationId conversationId, String userId);
}

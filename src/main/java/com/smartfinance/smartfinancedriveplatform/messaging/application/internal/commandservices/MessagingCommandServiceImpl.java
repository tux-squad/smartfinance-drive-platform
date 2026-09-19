package com.smartfinance.smartfinancedriveplatform.messaging.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.messaging.application.commandservices.MessagingCommandService;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates.Conversation;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.commands.CreateConversationCommand;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.commands.SendMessageCommand;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.entities.Message;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.repositories.ConversationRepository;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.repositories.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MessagingCommandServiceImpl implements MessagingCommandService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public MessagingCommandServiceImpl(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public Optional<Conversation> handle(CreateConversationCommand command) {
        var existingOpt = conversationRepository.findByBuyerUserIdAndDealerUserId(command.buyerUserId(), command.dealerUserId());
        Conversation conversation;
        if (existingOpt.isPresent()) {
            conversation = existingOpt.get();
        } else {
            conversation = new Conversation(command.buyerUserId(), command.dealerUserId(), command.vehicleId());
            conversation = conversationRepository.save(conversation);
        }

        if (command.initialMessage() != null && !command.initialMessage().isBlank()) {
            Message initialMessage = new Message(conversation.getId().value(), command.buyerUserId(), command.initialMessage(), null);
            messageRepository.save(initialMessage);
            conversation.updateLastMessage(command.initialMessage(), command.buyerUserId(), initialMessage.getSentAt());
            conversation = conversationRepository.save(conversation);
        }

        return Optional.of(conversation);
    }

    @Override
    @Transactional
    public Optional<Message> handle(SendMessageCommand command) {
        var conversationOpt = conversationRepository.findById(command.conversationId());
        if (conversationOpt.isEmpty()) {
            return Optional.empty();
        }
        var conversation = conversationOpt.get();
        Message message = new Message(conversation.getId().value(), command.senderUserId(), command.content(), command.attachmentUrl());
        Message savedMessage = messageRepository.save(message);

        conversation.updateLastMessage(command.content(), command.senderUserId(), savedMessage.getSentAt());
        conversationRepository.save(conversation);

        return Optional.of(savedMessage);
    }

    @Override
    @Transactional
    public void markConversationAsRead(ConversationId conversationId, String userId) {
        var conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isPresent()) {
            var conversation = conversationOpt.get();
            conversation.markAsReadForUser(userId);
            conversationRepository.save(conversation);
            messageRepository.markAllAsReadForConversation(conversationId, userId);
        }
    }
}

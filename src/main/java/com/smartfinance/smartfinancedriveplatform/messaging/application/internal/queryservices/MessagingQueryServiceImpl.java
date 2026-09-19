package com.smartfinance.smartfinancedriveplatform.messaging.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.messaging.application.queryservices.MessagingQueryService;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates.Conversation;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.entities.Message;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.queries.GetConversationsForUserQuery;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.queries.GetMessagesByConversationIdQuery;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.repositories.ConversationRepository;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.repositories.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MessagingQueryServiceImpl implements MessagingQueryService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public MessagingQueryServiceImpl(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> handle(GetConversationsForUserQuery query) {
        return conversationRepository.findAllByUserId(query.userId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> handle(GetMessagesByConversationIdQuery query) {
        return messageRepository.findAllByConversationIdOrderBySentAtAsc(query.conversationId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Conversation> getConversationById(ConversationId conversationId) {
        return conversationRepository.findById(conversationId);
    }
}

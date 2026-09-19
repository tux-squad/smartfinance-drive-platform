package com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.entities.Message;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.repositories.MessageRepository;
import com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.assemblers.MessagePersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.entities.MessagePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.repositories.SpringDataMessageRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageRepositoryAdapter implements MessageRepository {

    private final SpringDataMessageRepository repository;

    public MessageRepositoryAdapter(SpringDataMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public Message save(Message message) {
        MessagePersistenceEntity existing = repository.findById(message.getId().value()).orElse(null);
        MessagePersistenceEntity entityToSave = MessagePersistenceAssembler.toEntity(message, existing);
        MessagePersistenceEntity saved = repository.save(entityToSave);
        return MessagePersistenceAssembler.toDomain(saved);
    }

    @Override
    public List<Message> findAllByConversationIdOrderBySentAtAsc(ConversationId conversationId) {
        return repository.findAllByConversationIdOrderBySentAtAsc(conversationId.value()).stream()
                .map(MessagePersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void markAllAsReadForConversation(ConversationId conversationId, String excludeSenderUserId) {
        repository.markAllAsReadForConversation(conversationId.value(), excludeSenderUserId);
    }
}

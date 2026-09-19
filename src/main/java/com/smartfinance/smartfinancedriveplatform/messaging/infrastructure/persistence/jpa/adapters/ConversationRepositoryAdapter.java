package com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates.Conversation;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.repositories.ConversationRepository;
import com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.assemblers.ConversationPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.entities.ConversationPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.repositories.SpringDataConversationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final SpringDataConversationRepository repository;

    public ConversationRepositoryAdapter(SpringDataConversationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Conversation save(Conversation conversation) {
        ConversationPersistenceEntity existing = repository.findById(conversation.getId().value()).orElse(null);
        ConversationPersistenceEntity entityToSave = ConversationPersistenceAssembler.toEntity(conversation, existing);
        ConversationPersistenceEntity saved = repository.save(entityToSave);
        return ConversationPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Conversation> findById(ConversationId id) {
        return repository.findById(id.value()).map(ConversationPersistenceAssembler::toDomain);
    }

    @Override
    public List<Conversation> findAllByUserId(String userId) {
        return repository.findAllByUserId(userId).stream()
                .map(ConversationPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Conversation> findByBuyerUserIdAndDealerUserId(String buyerUserId, String dealerUserId) {
        return repository.findByBuyerUserIdAndDealerUserId(buyerUserId, dealerUserId)
                .map(ConversationPersistenceAssembler::toDomain);
    }
}

package com.smartfinance.smartfinancedriveplatform.messaging.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates.Conversation;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository {
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(ConversationId id);
    List<Conversation> findAllByUserId(String userId);
    Optional<Conversation> findByBuyerUserIdAndDealerUserId(String buyerUserId, String dealerUserId);
}

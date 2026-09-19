package com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.entities.ConversationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataConversationRepository extends JpaRepository<ConversationPersistenceEntity, UUID> {

    @Query("SELECT c FROM ConversationPersistenceEntity c WHERE c.buyerUserId = :userId OR c.dealerUserId = :userId ORDER BY c.lastMessageTimestamp DESC")
    List<ConversationPersistenceEntity> findAllByUserId(@Param("userId") String userId);

    Optional<ConversationPersistenceEntity> findByBuyerUserIdAndDealerUserId(String buyerUserId, String dealerUserId);
}

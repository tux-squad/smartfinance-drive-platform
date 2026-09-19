package com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.entities.MessagePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataMessageRepository extends JpaRepository<MessagePersistenceEntity, UUID> {

    List<MessagePersistenceEntity> findAllByConversationIdOrderBySentAtAsc(UUID conversationId);

    @Modifying
    @Query("UPDATE MessagePersistenceEntity m SET m.read = true WHERE m.conversationId = :conversationId AND m.senderUserId <> :excludeSenderUserId")
    void markAllAsReadForConversation(@Param("conversationId") UUID conversationId, @Param("excludeSenderUserId") String excludeSenderUserId);
}

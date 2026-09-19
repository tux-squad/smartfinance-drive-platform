package com.smartfinance.smartfinancedriveplatform.messaging.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conv_buyer", columnList = "buyer_user_id"),
        @Index(name = "idx_conv_dealer", columnList = "dealer_user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ConversationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "buyer_user_id", nullable = false)
    private String buyerUserId;

    @Column(name = "dealer_user_id", nullable = false)
    private String dealerUserId;

    @Column(name = "vehicle_id")
    private UUID vehicleId;

    @Column(name = "last_message_content", length = 1000)
    private String lastMessageContent;

    @Column(name = "last_message_timestamp")
    private Instant lastMessageTimestamp;

    @Column(name = "unread_buyer_count", nullable = false)
    private int unreadBuyerCount = 0;

    @Column(name = "unread_dealer_count", nullable = false)
    private int unreadDealerCount = 0;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}

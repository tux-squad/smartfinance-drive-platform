package com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Conversation Aggregate Root Unit Tests")
class ConversationTest {

    @Test
    @DisplayName("Should create conversation and increment unread counts on message update")
    void shouldCreateAndUpdateConversation() {
        Conversation conversation = new Conversation("buyer-1", "dealer-1", UUID.randomUUID());

        assertThat(conversation.getId()).isNotNull();
        assertThat(conversation.getBuyerUserId()).isEqualTo("buyer-1");
        assertThat(conversation.getDealerUserId()).isEqualTo("dealer-1");
        assertThat(conversation.getUnreadBuyerCount()).isZero();
        assertThat(conversation.getUnreadDealerCount()).isZero();

        conversation.updateLastMessage("Hola, me interesa el auto", "buyer-1", Instant.now());
        assertThat(conversation.getUnreadDealerCount()).isEqualTo(1);

        conversation.markAsReadForUser("dealer-1");
        assertThat(conversation.getUnreadDealerCount()).isZero();
    }
}

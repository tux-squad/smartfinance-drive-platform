package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.messaging.application.commandservices.MessagingCommandService;
import com.smartfinance.smartfinancedriveplatform.messaging.application.queryservices.MessagingQueryService;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.aggregates.Conversation;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.queries.GetConversationsForUserQuery;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.queries.GetMessagesByConversationIdQuery;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ConversationsController Unit Tests")
class ConversationsControllerTest {

    private MessagingCommandService commandService;
    private MessagingQueryService queryService;
    private SimpMessagingTemplate messagingTemplate;
    private ConversationsController controller;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        commandService = mock(MessagingCommandService.class);
        queryService = mock(MessagingQueryService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        controller = new ConversationsController(commandService, queryService, messagingTemplate);

        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getRequiredCurrentUserId).thenReturn("buyer-123");
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("Should get user conversations successfully")
    void shouldGetUserConversations() {
        Conversation conversation = new Conversation("buyer-123", "dealer-456", UUID.randomUUID());
        when(queryService.handle(any(GetConversationsForUserQuery.class))).thenReturn(List.of(conversation));

        var response = controller.getMyConversations();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("Should return conversation messages when found")
    void shouldReturnConversationMessagesWhenFound() {
        UUID conversationId = UUID.randomUUID();
        when(queryService.handle(any(GetMessagesByConversationIdQuery.class))).thenReturn(Collections.emptyList());

        var response = controller.getConversationMessages(conversationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}

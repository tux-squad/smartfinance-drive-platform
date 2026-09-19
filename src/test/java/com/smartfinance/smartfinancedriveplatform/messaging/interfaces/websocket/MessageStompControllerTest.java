package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.websocket;

import com.smartfinance.smartfinancedriveplatform.messaging.application.commandservices.MessagingCommandService;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.entities.Message;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.commands.SendMessageCommand;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources.MessageResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageStompController Unit Tests")
class MessageStompControllerTest {

    @Mock
    private MessagingCommandService commandService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageStompController controller;

    @Test
    @DisplayName("Should process message and broadcast via SimpMessagingTemplate")
    void shouldProcessMessageAndBroadcast() {
        UUID conversationId = UUID.randomUUID();
        StompMessageRequest request = new StompMessageRequest(conversationId, "user-1", "Hello via STOMP");
        Message sampleMessage = new Message(conversationId, "user-1", "Hello via STOMP", null);

        when(commandService.handle(any(SendMessageCommand.class))).thenReturn(Optional.of(sampleMessage));

        controller.processMessage(request);

        verify(commandService, times(1)).handle(any(SendMessageCommand.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/conversations/" + conversationId), any(MessageResource.class));
    }

    @Test
    @DisplayName("Should ignore null request")
    void shouldIgnoreNullRequest() {
        controller.processMessage(null);
        verifyNoInteractions(commandService);
        verifyNoInteractions(messagingTemplate);
    }
}

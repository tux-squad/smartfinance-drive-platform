package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.websocket;

import com.smartfinance.smartfinancedriveplatform.messaging.application.commandservices.MessagingCommandService;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.commands.SendMessageCommand;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources.MessageResource;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.transform.MessageResourceFromEntityAssembler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller handling inbound STOMP WebSocket frames mapped to /app/chat.sendMessage.
 */
@Controller
public class MessageStompController {

    private final MessagingCommandService commandService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageStompController(MessagingCommandService commandService, SimpMessagingTemplate messagingTemplate) {
        this.commandService = commandService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload StompMessageRequest request) {
        if (request == null || request.conversationId() == null || request.senderUserId() == null || request.content() == null) {
            return;
        }

        var command = new SendMessageCommand(
                new ConversationId(request.conversationId()),
                request.senderUserId(),
                request.content(),
                null
        );
        var msgOpt = commandService.handle(command);
        if (msgOpt.isPresent()) {
            MessageResource resource = MessageResourceFromEntityAssembler.toResourceFromEntity(msgOpt.get());
            messagingTemplate.convertAndSend("/topic/conversations/" + request.conversationId(), resource);
        }
    }
}

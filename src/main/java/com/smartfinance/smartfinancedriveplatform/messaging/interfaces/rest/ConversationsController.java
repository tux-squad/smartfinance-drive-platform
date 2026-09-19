package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.messaging.application.commandservices.MessagingCommandService;
import com.smartfinance.smartfinancedriveplatform.messaging.application.queryservices.MessagingQueryService;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.commands.CreateConversationCommand;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.commands.SendMessageCommand;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.queries.GetConversationsForUserQuery;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.queries.GetMessagesByConversationIdQuery;
import com.smartfinance.smartfinancedriveplatform.messaging.domain.model.valueobjects.ConversationId;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources.ConversationResource;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources.CreateConversationResource;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources.MessageResource;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources.SendMessageResource;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.transform.ConversationResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.transform.MessageResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for chat conversations and message history.
 */
@RestController
@RequestMapping(value = "/api/v1/conversations", produces = "application/json")
public class ConversationsController {

    private final MessagingCommandService commandService;
    private final MessagingQueryService queryService;
    private final SimpMessagingTemplate messagingTemplate;

    public ConversationsController(MessagingCommandService commandService,
                                   MessagingQueryService queryService,
                                   SimpMessagingTemplate messagingTemplate) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * GET /api/v1/conversations
     * Retrieves active conversations for the authenticated user.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationResource>> getMyConversations() {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var query = new GetConversationsForUserQuery(authUserId);
        var conversations = queryService.handle(query);
        var resources = conversations.stream()
                .map(ConversationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * GET /api/v1/conversations/{id}/messages
     * Retrieves message history of a conversation.
     */
    @GetMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageResource>> getConversationMessages(@PathVariable UUID id) {
        var query = new GetMessagesByConversationIdQuery(new ConversationId(id));
        var messages = queryService.handle(query);
        var resources = messages.stream()
                .map(MessageResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    /**
     * POST /api/v1/conversations
     * Starts a new conversation between buyer and dealer.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResource> createConversation(
            @jakarta.validation.Valid @RequestBody CreateConversationResource resource) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new CreateConversationCommand(
                authUserId,
                resource.dealerUserId(),
                resource.vehicleId(),
                resource.initialMessage()
        );
        var conversationOpt = commandService.handle(command);
        return conversationOpt
                .map(c -> new ResponseEntity<>(ConversationResourceFromEntityAssembler.toResourceFromEntity(c), HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * POST /api/v1/conversations/{id}/messages
     * Sends a new message in a conversation. Broadcasts to WebSocket topic /topic/conversations/{id}.
     */
    @PostMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResource> sendMessage(
            @PathVariable UUID id,
            @jakarta.validation.Valid @RequestBody SendMessageResource resource) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new SendMessageCommand(
                new ConversationId(id),
                authUserId,
                resource.content(),
                resource.attachmentUrl()
        );
        var messageOpt = commandService.handle(command);
        if (messageOpt.isPresent()) {
            var msgResource = MessageResourceFromEntityAssembler.toResourceFromEntity(messageOpt.get());
            messagingTemplate.convertAndSend("/topic/conversations/" + id, msgResource);
            return ResponseEntity.status(HttpStatus.CREATED).body(msgResource);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * POST /api/v1/conversations/{id}/read
     * Marks conversation unread counts as read for caller.
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        String authUserId = SecurityUtils.getRequiredCurrentUserId();
        commandService.markConversationAsRead(new ConversationId(id), authUserId);
        return ResponseEntity.ok().build();
    }
}

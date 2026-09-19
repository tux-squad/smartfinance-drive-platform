package com.smartfinance.smartfinancedriveplatform.messaging.domain.model.commands;

import java.util.UUID;

public record CreateConversationCommand(
    String buyerUserId,
    String dealerUserId,
    UUID vehicleId,
    String initialMessage
) {}

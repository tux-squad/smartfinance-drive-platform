package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateConversationResource(
    @NotBlank(message = "Dealer user ID is required")
    String dealerUserId,

    UUID vehicleId,

    String initialMessage
) {}

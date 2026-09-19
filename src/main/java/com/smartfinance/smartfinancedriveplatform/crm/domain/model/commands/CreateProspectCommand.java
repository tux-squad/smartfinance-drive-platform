package com.smartfinance.smartfinancedriveplatform.crm.domain.model.commands;

import java.util.UUID;

public record CreateProspectCommand(
    String dealerUserId,
    String buyerUserId,
    String fullName,
    String email,
    String phone,
    UUID interestedVehicleId,
    String salesAgentId
) {}

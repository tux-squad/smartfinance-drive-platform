package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

import java.util.UUID;

public record UpdateSalesAgentCommand(
        UUID agentId,
        String dealerUserId,
        String fullName,
        String email,
        String phone,
        boolean active
) {
    public UpdateSalesAgentCommand {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId cannot be null");
        }
        if (dealerUserId == null || dealerUserId.isBlank()) {
            throw new IllegalArgumentException("dealerUserId cannot be null or empty");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName cannot be null or empty");
        }
    }
}

package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

import java.util.UUID;

public record ReassignLeadsCommand(
        UUID sourceAgentId,
        UUID targetAgentId,
        String dealerUserId
) {
    public ReassignLeadsCommand {
        if (sourceAgentId == null) {
            throw new IllegalArgumentException("sourceAgentId cannot be null");
        }
        if (targetAgentId == null) {
            throw new IllegalArgumentException("targetAgentId cannot be null");
        }
        if (dealerUserId == null || dealerUserId.isBlank()) {
            throw new IllegalArgumentException("dealerUserId cannot be null or empty");
        }
    }
}

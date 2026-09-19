package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

public record CreateSalesAgentCommand(
        String dealerUserId,
        String fullName,
        String email,
        String phone
) {
    public CreateSalesAgentCommand {
        if (dealerUserId == null || dealerUserId.isBlank()) {
            throw new IllegalArgumentException("dealerUserId cannot be null or empty");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName cannot be null or empty");
        }
    }
}

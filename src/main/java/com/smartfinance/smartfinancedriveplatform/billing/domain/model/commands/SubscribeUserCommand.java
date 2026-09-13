package com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands;

/**
 * Command representing a request to subscribe a user/organization to a plan.
 */
public record SubscribeUserCommand(
        String userId,
        Long planId,
        boolean autoRenew
) {
    public SubscribeUserCommand {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (planId == null || planId <= 0) {
            throw new IllegalArgumentException("planId must be greater than zero");
        }
    }
}

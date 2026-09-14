package com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands;

/**
 * Command representing a request to cancel an active subscription.
 */
public record CancelSubscriptionCommand(
        Long subscriptionId,
        String userId
) {
    public CancelSubscriptionCommand {
        if (subscriptionId == null || subscriptionId <= 0) {
            throw new IllegalArgumentException("subscriptionId must be greater than zero");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
    }
}

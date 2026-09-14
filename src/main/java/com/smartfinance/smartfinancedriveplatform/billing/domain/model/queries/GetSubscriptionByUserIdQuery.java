package com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries;

/**
 * Query to retrieve active subscription by user ID.
 */
public record GetSubscriptionByUserIdQuery(String userId) {
    public GetSubscriptionByUserIdQuery {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
    }
}

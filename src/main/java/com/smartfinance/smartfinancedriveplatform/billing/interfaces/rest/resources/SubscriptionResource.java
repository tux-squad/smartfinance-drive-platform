package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;

import java.time.LocalDateTime;

public record SubscriptionResource(
        Long id,
        @JsonIgnore String userId,
        PlanResource plan,
        SubscriptionStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean autoRenew,
        boolean active
) {}

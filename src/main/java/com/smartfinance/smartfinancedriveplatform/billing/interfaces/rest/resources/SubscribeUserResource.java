package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

public record SubscribeUserResource(
        @NotNull Long planId,
        boolean autoRenew
) {}

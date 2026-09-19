package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import java.util.UUID;

public record SalesAgentResource(
        UUID id,
        String dealerUserId,
        String fullName,
        String email,
        String phone,
        boolean active
) {
}

package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for updating a user's role.
 */
public record UpdateUserRoleResource(
        @NotNull(message = "Role is required")
        @NotBlank(message = "Role must not be blank")
        String role
) {}

package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;

/**
 * Command representing an administrative request to update a user's role.
 */
public record UpdateUserRoleCommand(Long userId, Roles role) {
    public UpdateUserRoleCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than zero");
        }
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
    }
}

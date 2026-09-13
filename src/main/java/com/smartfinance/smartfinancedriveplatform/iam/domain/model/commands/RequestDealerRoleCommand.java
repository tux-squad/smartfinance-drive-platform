package com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands;

/**
 * Command representing a request to automatically verify SUNAT RUC and assign ROLE_DEALER.
 */
public record RequestDealerRoleCommand(Long userId, String ruc) {
    public RequestDealerRoleCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than zero");
        }
        if (ruc == null || !ruc.matches("^\\d{11}$")) {
            throw new IllegalArgumentException("RUC must be exactly 11 numeric digits");
        }
    }
}

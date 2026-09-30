package com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands;

/**
 * Command to link or automatically provision a Dealership aggregate to a verified user.
 */
public record LinkDealershipToUserCommand(
        String userId,
        String ruc,
        String name,
        String address,
        String email
) {}

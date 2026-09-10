package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;

/**
 * Command to request the deletion of a customer profile.
 */
public record DeleteProfileCommand(ProfileId profileId) {}

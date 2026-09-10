package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;

/**
 * Query to retrieve a single profile by its profile ID.
 */
public record GetProfileByIdQuery(ProfileId profileId) {}

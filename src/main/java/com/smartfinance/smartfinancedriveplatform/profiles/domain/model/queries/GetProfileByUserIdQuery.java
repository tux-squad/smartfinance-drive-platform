package com.smartfinance.smartfinancedriveplatform.profiles.domain.model.queries;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;

/**
 * Query to retrieve a profile by its associated User ID.
 */
public record GetProfileByUserIdQuery(UserId userId) {}

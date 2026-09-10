package com.smartfinance.smartfinancedriveplatform.profiles.application.queryservices;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates.Profile;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.queries.GetProfileByIdQuery;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.queries.GetProfileByUserIdQuery;

import java.util.Optional;

/**
 * Interface declaring query operations for the Profile application layer.
 */
public interface ProfileQueryService {

    /**
     * Handles retrieving a profile by its unique profile ID.
     *
     * @param query The query containing the profile ID.
     * @return An Optional containing the profile if found, or empty.
     */
    Optional<Profile> handle(GetProfileByIdQuery query);

    /**
     * Handles retrieving a profile by its associated user ID.
     *
     * @param query The query containing the user ID.
     * @return An Optional containing the profile if found, or empty.
     */
    Optional<Profile> handle(GetProfileByUserIdQuery query);
}

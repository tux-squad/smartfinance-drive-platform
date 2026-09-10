package com.smartfinance.smartfinancedriveplatform.profiles.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates.Profile;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;

import java.util.Optional;

/**
 * Domain repository interface for Profile operations.
 */
public interface ProfileRepository {

    /**
     * Saves a Profile aggregate.
     *
     * @param profile The profile aggregate to save.
     * @return The saved profile aggregate.
     */
    Profile save(Profile profile);

    /**
     * Finds a Profile by its unique identifier.
     *
     * @param id The profile ID.
     * @return An Optional containing the profile if found, or empty.
     */
    Optional<Profile> findById(ProfileId id);

    /**
     * Finds a Profile by its associated User ID.
     *
     * @param userId The user ID.
     * @return An Optional containing the profile if found, or empty.
     */
    Optional<Profile> findByUserId(UserId userId);

    /**
     * Checks if a Profile exists by its identifier.
     *
     * @param id The profile ID.
     * @return true if it exists, false otherwise.
     */
    boolean existsById(ProfileId id);

    /**
     * Checks if a Profile exists for a specific user ID.
     *
     * @param userId The user ID.
     * @return true if it exists, false otherwise.
     */
    boolean existsByUserId(UserId userId);

    /**
     * Deletes a Profile by its identifier.
     *
     * @param id The profile ID.
     */
    void deleteById(ProfileId id);
}

package com.smartfinance.smartfinancedriveplatform.profiles.application.commandservices;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates.Profile;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands.CreateProfileCommand;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands.DeleteProfileCommand;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands.UpdateProfileCommand;

import java.util.Optional;

/**
 * Interface declaring command operations for the Profile application layer.
 */
public interface ProfileCommandService {

    /**
     * Handles the creation of a new customer profile.
     *
     * @param command The creation command.
     * @return An Optional containing the created profile.
     */
    Optional<Profile> handle(CreateProfileCommand command);

    /**
     * Handles updating an existing customer profile.
     *
     * @param command The update command.
     * @return An Optional containing the updated profile if found, or empty.
     */
    Optional<Profile> handle(UpdateProfileCommand command);

    /**
     * Handles deleting a customer profile.
     *
     * @param command The deletion command.
     */
    void handle(DeleteProfileCommand command);
}

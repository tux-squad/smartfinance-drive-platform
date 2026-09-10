package com.smartfinance.smartfinancedriveplatform.profiles.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.profiles.application.commandservices.ProfileCommandService;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates.Profile;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands.CreateProfileCommand;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands.DeleteProfileCommand;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands.UpdateProfileCommand;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.repositories.ProfileRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of ProfileCommandService application service.
 * Handles transactional business logic for profile mutation operations.
 */
@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {

    private final ProfileRepository profileRepository;

    public ProfileCommandServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public Optional<Profile> handle(CreateProfileCommand command) {
        if (profileRepository.existsByUserId(command.userId())) {
            throw new DomainValidationException("profiles.error.userAlreadyHasProfile");
        }

        Profile profile = new Profile(
            command.userId(),
            command.email(),
            command.nationalId(),
            command.fullLegalNames(),
            command.dateOfBirth(),
            command.phoneCountryCode(),
            command.mobilePhone(),
            command.monthlyIncome(),
            command.employmentStatus()
        );
        Profile savedProfile = profileRepository.save(profile);
        return Optional.of(savedProfile);
    }

    @Override
    @Transactional
    public Optional<Profile> handle(UpdateProfileCommand command) {
        var profileOpt = profileRepository.findById(command.profileId());
        if (profileOpt.isEmpty()) {
            return Optional.empty();
        }

        Profile profile = profileOpt.get();
        profile.updateDetails(
            command.email(),
            command.nationalId(),
            command.fullLegalNames(),
            command.dateOfBirth(),
            command.phoneCountryCode(),
            command.mobilePhone(),
            command.monthlyIncome(),
            command.employmentStatus()
        );
        Profile savedProfile = profileRepository.save(profile);
        return Optional.of(savedProfile);
    }

    @Override
    @Transactional
    public void handle(DeleteProfileCommand command) {
        if (!profileRepository.existsById(command.profileId())) {
            throw new DomainValidationException("profiles.error.profileNotFound");
        }
        profileRepository.deleteById(command.profileId());
    }
}

package com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates.Profile;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.repositories.ProfileRepository;
import com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.assemblers.ProfilePersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.entities.ProfilePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.repositories.SpringDataProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter implementing the ProfileRepository interface from the domain layer
 * by delegating to Spring Data JPA and assembling results.
 */
@Component
public class ProfileRepositoryAdapter implements ProfileRepository {

    private final SpringDataProfileRepository springDataProfileRepository;

    public ProfileRepositoryAdapter(SpringDataProfileRepository springDataProfileRepository) {
        this.springDataProfileRepository = springDataProfileRepository;
    }

    @Override
    public Profile save(Profile profile) {
        ProfilePersistenceEntity existingEntity = springDataProfileRepository.findById(profile.getId().value()).orElse(null);
        ProfilePersistenceEntity entityToSave = ProfilePersistenceAssembler.toEntity(profile, existingEntity);
        ProfilePersistenceEntity savedEntity = springDataProfileRepository.save(entityToSave);
        return ProfilePersistenceAssembler.toDomain(savedEntity);
    }

    @Override
    public Optional<Profile> findById(ProfileId id) {
        return springDataProfileRepository.findById(id.value())
                .map(ProfilePersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Profile> findByUserId(UserId userId) {
        return springDataProfileRepository.findByUserId(userId.value())
                .map(ProfilePersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsById(ProfileId id) {
        return springDataProfileRepository.existsById(id.value());
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return springDataProfileRepository.existsByUserId(userId.value());
    }

    @Override
    public void deleteById(ProfileId id) {
        springDataProfileRepository.deleteById(id.value());
    }
}

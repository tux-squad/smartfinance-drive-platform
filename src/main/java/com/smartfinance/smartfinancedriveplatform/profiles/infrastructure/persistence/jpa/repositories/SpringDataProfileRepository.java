package com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.entities.ProfilePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for Profile persistence operations.
 */
public interface SpringDataProfileRepository extends JpaRepository<ProfilePersistenceEntity, UUID> {

    /**
     * Finds a profile entity by its associated User ID.
     *
     * @param userId The user UUID.
     * @return An Optional containing the profile entity if found.
     */
    Optional<ProfilePersistenceEntity> findByUserId(String userId);

    /**
     * Checks if a profile entity exists for a specific user ID.
     *
     * @param userId The user UUID.
     * @return true if it exists, false otherwise.
     */
    boolean existsByUserId(String userId);
}

package com.forkdevs.driveos.platform.core.infrastructure.persistence.jpa.repositories;

import com.forkdevs.driveos.platform.core.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanPersistenceRepository extends JpaRepository<SubscriptionPlanPersistenceEntity, UUID> {
    Optional<SubscriptionPlanPersistenceEntity> findByName(String name);
}

package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for RolePersistenceEntity.
 */
@Repository
public interface SpringDataRoleRepository extends JpaRepository<RolePersistenceEntity, Long> {

    Optional<RolePersistenceEntity> findByName(Roles name);

    boolean existsByName(Roles name);
}

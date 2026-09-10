package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.UserJPAEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserJPAEntity}.
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJPAEntity, Long> {

    Optional<UserJPAEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}

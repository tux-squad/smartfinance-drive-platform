package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.UserJPAEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserJPAEntity}.
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJPAEntity, Long> {
    
    @Query("SELECT u FROM UserJPAEntity u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<UserJPAEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query(value = "SELECT DISTINCT u FROM UserJPAEntity u LEFT JOIN FETCH u.roles",
           countQuery = "SELECT COUNT(u) FROM UserJPAEntity u")
    Page<UserJPAEntity> findAllPaged(Pageable pageable);
}

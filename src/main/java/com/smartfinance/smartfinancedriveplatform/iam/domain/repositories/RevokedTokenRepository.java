package com.smartfinance.smartfinancedriveplatform.iam.domain.repositories;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.entities.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for RevokedToken entity.
 */
@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByTokenIdentifier(String tokenIdentifier);
    Optional<RevokedToken> findByTokenIdentifier(String tokenIdentifier);
    void deleteByExpirationTimeMillisLessThan(Long currentTimeMillis);
}

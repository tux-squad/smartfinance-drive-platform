package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.repositories;

import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities.DealershipPersistenceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for DealershipPersistenceEntity.
 */
public interface SpringDataDealershipRepository extends JpaRepository<DealershipPersistenceEntity, UUID> {

    Optional<DealershipPersistenceEntity> findByUserId(String userId);

    @Query("SELECT d FROM DealershipPersistenceEntity d WHERE d.active = true AND " +
           "(:search IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.ruc) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DealershipPersistenceEntity> findAllActiveWithSearch(@Param("search") String search, Pageable pageable);
}

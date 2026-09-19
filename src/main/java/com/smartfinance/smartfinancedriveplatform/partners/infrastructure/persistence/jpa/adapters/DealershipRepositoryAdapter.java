package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.assemblers.DealershipPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities.DealershipPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.repositories.SpringDataDealershipRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter implementing DealershipRepository delegating to Spring Data JPA.
 */
@Component
public class DealershipRepositoryAdapter implements DealershipRepository {

    private final SpringDataDealershipRepository repository;

    public DealershipRepositoryAdapter(SpringDataDealershipRepository repository) {
        this.repository = repository;
    }

    @Override
    public Dealership save(Dealership dealership) {
        DealershipPersistenceEntity existing = repository.findById(dealership.getId().value()).orElse(null);
        DealershipPersistenceEntity entityToSave = DealershipPersistenceAssembler.toEntity(dealership, existing);
        DealershipPersistenceEntity saved = repository.save(entityToSave);
        return DealershipPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Dealership> findById(DealershipId id) {
        return repository.findById(id.value()).map(DealershipPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Dealership> findByUserId(String userId) {
        return repository.findByUserId(userId).map(DealershipPersistenceAssembler::toDomain);
    }

    @Override
    public Page<Dealership> findAll(String search, Pageable pageable) {
        return repository.findAllActiveWithSearch(search, pageable).map(DealershipPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsById(DealershipId id) {
        return repository.existsById(id.value());
    }
}

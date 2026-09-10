package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.assemblers.FinancialEntityPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities.FinancialEntityPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.repositories.SpringDataFinancialEntityRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing the FinancialEntityRepository interface from the domain layer
 * by delegating to Spring Data JPA and assembling results.
 */
@Component
public class FinancialEntityRepositoryAdapter implements FinancialEntityRepository {

    private final SpringDataFinancialEntityRepository springDataFinancialEntityRepository;

    public FinancialEntityRepositoryAdapter(SpringDataFinancialEntityRepository springDataFinancialEntityRepository) {
        this.springDataFinancialEntityRepository = springDataFinancialEntityRepository;
    }

    @Override
    public FinancialEntity save(FinancialEntity financialEntity) {
        FinancialEntityPersistenceEntity existingEntity = springDataFinancialEntityRepository.findById(financialEntity.getId().value()).orElse(null);
        FinancialEntityPersistenceEntity entityToSave = FinancialEntityPersistenceAssembler.toEntity(financialEntity, existingEntity);
        FinancialEntityPersistenceEntity savedEntity = springDataFinancialEntityRepository.save(entityToSave);
        return FinancialEntityPersistenceAssembler.toDomain(savedEntity);
    }

    @Override
    public Optional<FinancialEntity> findById(FinancialEntityId id) {
        return springDataFinancialEntityRepository.findById(id.value())
                .map(FinancialEntityPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<FinancialEntity> findByName(String name) {
        return springDataFinancialEntityRepository.findByName(name)
                .map(FinancialEntityPersistenceAssembler::toDomain);
    }

    @Override
    public List<FinancialEntity> findAll() {
        return springDataFinancialEntityRepository.findAll().stream()
                .map(FinancialEntityPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(FinancialEntityId id) {
        return springDataFinancialEntityRepository.existsById(id.value());
    }

    @Override
    public boolean existsByName(String name) {
        return springDataFinancialEntityRepository.existsByName(name);
    }

    @Override
    public void deleteById(FinancialEntityId id) {
        springDataFinancialEntityRepository.deleteById(id.value());
    }
}

package com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.CreditApplicationId;
import com.smartfinance.smartfinancedriveplatform.financing.domain.repositories.CreditApplicationRepository;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.assemblers.CreditApplicationPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities.CreditApplicationPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.repositories.SpringDataCreditApplicationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing CreditApplicationRepository delegating to Spring Data JPA.
 */
@Component
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepository {

    private final SpringDataCreditApplicationRepository repository;

    public CreditApplicationRepositoryAdapter(SpringDataCreditApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public CreditApplication save(CreditApplication creditApplication) {
        CreditApplicationPersistenceEntity existing = repository.findById(creditApplication.getId().value()).orElse(null);
        CreditApplicationPersistenceEntity entityToSave = CreditApplicationPersistenceAssembler.toEntity(creditApplication, existing);
        CreditApplicationPersistenceEntity saved = repository.save(entityToSave);
        return CreditApplicationPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<CreditApplication> findById(CreditApplicationId id) {
        return repository.findById(id.value()).map(CreditApplicationPersistenceAssembler::toDomain);
    }

    @Override
    public List<CreditApplication> findAllByApplicantUserId(String applicantUserId) {
        return repository.findAllByApplicantUserId(applicantUserId).stream()
                .map(CreditApplicationPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(CreditApplicationId id) {
        return repository.existsById(id.value());
    }
}

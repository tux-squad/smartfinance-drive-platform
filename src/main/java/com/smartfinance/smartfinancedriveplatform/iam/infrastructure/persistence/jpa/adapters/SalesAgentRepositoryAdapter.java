package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.SalesAgent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.SalesAgentId;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.SalesAgentRepository;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.assemblers.SalesAgentPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.entities.SalesAgentPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories.SpringDataSalesAgentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SalesAgentRepositoryAdapter implements SalesAgentRepository {

    private final SpringDataSalesAgentRepository repository;

    public SalesAgentRepositoryAdapter(SpringDataSalesAgentRepository repository) {
        this.repository = repository;
    }

    @Override
    public SalesAgent save(SalesAgent salesAgent) {
        SalesAgentPersistenceEntity entity = SalesAgentPersistenceAssembler.toEntity(salesAgent);
        SalesAgentPersistenceEntity saved = repository.save(entity);
        return SalesAgentPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<SalesAgent> findById(SalesAgentId id) {
        return repository.findById(id.value())
                .map(SalesAgentPersistenceAssembler::toDomain);
    }

    @Override
    public List<SalesAgent> findByDealerUserId(String dealerUserId) {
        return repository.findByDealerUserId(dealerUserId).stream()
                .map(SalesAgentPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }
}

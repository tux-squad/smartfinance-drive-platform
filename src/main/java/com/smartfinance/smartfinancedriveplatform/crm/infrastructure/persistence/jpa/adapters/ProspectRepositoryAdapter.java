package com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.ProspectId;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.ProspectRepository;
import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.assemblers.ProspectPersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.entities.ProspectPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.repositories.SpringDataProspectRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProspectRepositoryAdapter implements ProspectRepository {

    private final SpringDataProspectRepository repository;

    public ProspectRepositoryAdapter(SpringDataProspectRepository repository) {
        this.repository = repository;
    }

    @Override
    public Prospect save(Prospect prospect) {
        ProspectPersistenceEntity existing = repository.findById(prospect.getId().value()).orElse(null);
        ProspectPersistenceEntity entityToSave = ProspectPersistenceAssembler.toEntity(prospect, existing);
        ProspectPersistenceEntity saved = repository.save(entityToSave);
        return ProspectPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Prospect> findById(ProspectId id) {
        return repository.findById(id.value()).map(ProspectPersistenceAssembler::toDomain);
    }

    @Override
    public List<Prospect> findAllByDealerUserId(String dealerUserId) {
        return repository.findAllByDealerUserIdOrderByCreatedAtDesc(dealerUserId).stream()
                .map(ProspectPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(ProspectId id) {
        return repository.existsById(id.value());
    }
}

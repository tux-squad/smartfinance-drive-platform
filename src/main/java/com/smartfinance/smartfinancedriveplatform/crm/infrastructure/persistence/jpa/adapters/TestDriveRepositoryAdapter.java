package com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.TestDriveRepository;
import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.assemblers.TestDrivePersistenceAssembler;
import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.entities.TestDrivePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.crm.infrastructure.persistence.jpa.repositories.SpringDataTestDriveRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TestDriveRepositoryAdapter implements TestDriveRepository {

    private final SpringDataTestDriveRepository repository;

    public TestDriveRepositoryAdapter(SpringDataTestDriveRepository repository) {
        this.repository = repository;
    }

    @Override
    public TestDrive save(TestDrive testDrive) {
        TestDrivePersistenceEntity existing = repository.findById(testDrive.getId().value()).orElse(null);
        TestDrivePersistenceEntity entityToSave = TestDrivePersistenceAssembler.toEntity(testDrive, existing);
        TestDrivePersistenceEntity saved = repository.save(entityToSave);
        return TestDrivePersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<TestDrive> findById(TestDriveId id) {
        return repository.findById(id.value()).map(TestDrivePersistenceAssembler::toDomain);
    }

    @Override
    public List<TestDrive> findAllByBuyerUserId(String buyerUserId) {
        return repository.findAllByBuyerUserId(buyerUserId).stream()
                .map(TestDrivePersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestDrive> findAllByDealershipId(UUID dealershipId) {
        return repository.findAllByDealershipId(dealershipId).stream()
                .map(TestDrivePersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }
}

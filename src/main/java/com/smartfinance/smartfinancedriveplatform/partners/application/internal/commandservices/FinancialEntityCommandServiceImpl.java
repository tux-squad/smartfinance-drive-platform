package com.smartfinance.smartfinancedriveplatform.partners.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.FinancialEntityCommandService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.AddRateBenchmarkCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.DeleteFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.UpdateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.entities.RateBenchmark;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of FinancialEntityCommandService application service.
 * Handles transactional mutation logic for financial entities and rate benchmarks.
 */
@Service
public class FinancialEntityCommandServiceImpl implements FinancialEntityCommandService {

    private final FinancialEntityRepository financialEntityRepository;

    public FinancialEntityCommandServiceImpl(FinancialEntityRepository financialEntityRepository) {
        this.financialEntityRepository = financialEntityRepository;
    }

    @Override
    @Transactional
    public Optional<FinancialEntity> handle(CreateFinancialEntityCommand command) {
        if (financialEntityRepository.existsByName(command.name())) {
            throw new DomainValidationException("partners.error.financialEntityAlreadyExists");
        }

        FinancialEntity financialEntity = new FinancialEntity(command.name());
        FinancialEntity savedEntity = financialEntityRepository.save(financialEntity);
        return Optional.of(savedEntity);
    }

    @Override
    @Transactional
    public Optional<FinancialEntity> handle(UpdateFinancialEntityCommand command) {
        var entityOpt = financialEntityRepository.findById(command.financialEntityId());
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        FinancialEntity financialEntity = entityOpt.get();
        financialEntity.setName(command.name());
        FinancialEntity savedEntity = financialEntityRepository.save(financialEntity);
        return Optional.of(savedEntity);
    }

    @Override
    @Transactional
    public Optional<FinancialEntity> handle(AddRateBenchmarkCommand command) {
        var entityOpt = financialEntityRepository.findById(command.financialEntityId());
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        FinancialEntity financialEntity = entityOpt.get();
        RateBenchmark benchmark = new RateBenchmark(
            command.rateType(),
            command.annualRate(),
            command.currency(),
            command.sourceLabel(),
            command.sourceUrl(),
            command.effectiveFrom()
        );
        financialEntity.addRateBenchmark(benchmark);
        FinancialEntity savedEntity = financialEntityRepository.save(financialEntity);
        return Optional.of(savedEntity);
    }

    @Override
    @Transactional
    public void handle(DeleteFinancialEntityCommand command) {
        if (!financialEntityRepository.existsById(command.financialEntityId())) {
            throw new DomainValidationException("partners.error.financialEntityNotFound");
        }
        financialEntityRepository.deleteById(command.financialEntityId());
    }
}

package com.smartfinance.smartfinancedriveplatform.partners.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.FinancialEntityCommandService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.AddRateBenchmarkCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.DeleteFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkFinancialEntityToUserCommand;
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
        if (command.ruc() != null && !command.ruc().isBlank()) {
            if (financialEntityRepository.existsByRuc(command.ruc())) {
                throw new DomainValidationException("partners.error.financialEntityRucAlreadyExists");
            }
        }

        if (financialEntityRepository.existsByName(command.name())) {
            var existingOpt = financialEntityRepository.findByName(command.name());
            if (existingOpt.isPresent() && existingOpt.get().getUserId() == null && command.userId() != null) {
                FinancialEntity existing = existingOpt.get();
                if (command.ruc() != null && existing.getRuc() != null && !existing.getRuc().equals(command.ruc())) {
                    throw new DomainValidationException("partners.error.financialEntityRucMismatch");
                }
                existing.updateDetails(command.name(), command.logoUrl(), command.bannerUrl(), command.userId(), command.ruc());
                FinancialEntity saved = financialEntityRepository.save(existing);
                return Optional.of(saved);
            }
            throw new DomainValidationException("partners.error.financialEntityAlreadyExists");
        }

        FinancialEntity financialEntity = new FinancialEntity(
                new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(java.util.UUID.randomUUID()),
                command.userId(),
                command.ruc(),
                command.name(),
                command.logoUrl(),
                command.bannerUrl(),
                new java.util.ArrayList<>()
        );
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
        String updatedUserId = (command.userId() != null && !command.userId().isBlank())
                ? command.userId()
                : financialEntity.getUserId();
        String updatedRuc = (command.ruc() != null && !command.ruc().isBlank())
                ? command.ruc()
                : financialEntity.getRuc();

        if (command.ruc() != null && !command.ruc().isBlank()) {
            var existingByRuc = financialEntityRepository.findByRuc(command.ruc());
            if (existingByRuc.isPresent() && !existingByRuc.get().getId().equals(financialEntity.getId())) {
                throw new DomainValidationException("partners.error.financialEntityRucAlreadyExists");
            }
        }

        financialEntity.updateDetails(command.name(), command.logoUrl(), command.bannerUrl(), updatedUserId, updatedRuc);
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
    public FinancialEntity handle(LinkFinancialEntityToUserCommand command) {
        String userId = command.userId();
        String ruc = command.ruc();
        String legalName = command.legalName();

        // 1. If user is already associated with an entity, return it (update RUC if empty)
        var existingByUserId = financialEntityRepository.findByUserId(userId);
        if (existingByUserId.isPresent()) {
            FinancialEntity entity = existingByUserId.get();
            if (entity.getRuc() == null && ruc != null && !ruc.isBlank()) {
                entity.setRuc(ruc);
                return financialEntityRepository.save(entity);
            }
            return entity;
        }

        // 2. Check if an entity already exists with this RUC
        if (ruc != null && !ruc.isBlank()) {
            var existingByRuc = financialEntityRepository.findByRuc(ruc);
            if (existingByRuc.isPresent()) {
                FinancialEntity entity = existingByRuc.get();
                if (entity.getUserId() != null && !entity.getUserId().equals(userId)) {
                    throw new DomainValidationException("partners.error.financialEntity.rucAlreadyLinked");
                }
                entity.setUserId(userId);
                return financialEntityRepository.save(entity);
            }
        }

        // 3. Check if an unlinked entity exists with this legal name (e.g. initial seed banks)
        if (legalName != null && !legalName.isBlank()) {
            var existingByName = financialEntityRepository.findByName(legalName);
            if (existingByName.isPresent()) {
                FinancialEntity entity = existingByName.get();
                if (entity.getUserId() == null) {
                    if (ruc != null && entity.getRuc() != null && !entity.getRuc().equals(ruc)) {
                        throw new DomainValidationException("partners.error.financialEntityRucMismatch");
                    }
                    entity.setUserId(userId);
                    if (entity.getRuc() == null && ruc != null && !ruc.isBlank()) {
                        entity.setRuc(ruc);
                    }
                    return financialEntityRepository.save(entity);
                } else if (!entity.getUserId().equals(userId)) {
                    throw new DomainValidationException("partners.error.financialEntityAlreadyExists");
                }
            }
        }

        // 4. Create new financial entity with verified RUC and legal name
        FinancialEntity newEntity = new FinancialEntity(
                new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(java.util.UUID.randomUUID()),
                userId,
                ruc,
                legalName,
                null,
                null,
                new java.util.ArrayList<>()
        );
        return financialEntityRepository.save(newEntity);
    }

    @Override
    @Transactional
    public Optional<FinancialEntity> updateLogo(com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId financialEntityId, String logoUrl) {
        var existingOpt = financialEntityRepository.findById(financialEntityId);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        var entity = existingOpt.get();
        entity.setLogoUrl(logoUrl);
        return Optional.of(financialEntityRepository.save(entity));
    }

    @Override
    @Transactional
    public Optional<FinancialEntity> updateBanner(com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId financialEntityId, String bannerUrl) {
        var existingOpt = financialEntityRepository.findById(financialEntityId);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        var entity = existingOpt.get();
        entity.setBannerUrl(bannerUrl);
        return Optional.of(financialEntityRepository.save(entity));
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

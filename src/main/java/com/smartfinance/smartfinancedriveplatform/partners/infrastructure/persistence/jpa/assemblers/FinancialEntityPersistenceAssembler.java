package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.entities.RateBenchmark;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.RateBenchmarkId;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities.FinancialEntityPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities.RateBenchmarkPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Assembler class to convert between the FinancialEntity domain model and JPA persistence entities.
 */
public final class FinancialEntityPersistenceAssembler {

    private FinancialEntityPersistenceAssembler() {}

    /**
     * Converts a FinancialEntity domain aggregate to a JPA persistence entity.
     *
     * @param domain The domain aggregate.
     * @param entity The target persistence entity (will create new if null).
     * @return The updated persistence entity.
     */
    public static FinancialEntityPersistenceEntity toEntity(FinancialEntity domain, FinancialEntityPersistenceEntity entity) {
        if (entity == null) {
            entity = new FinancialEntityPersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setName(domain.getName());

        // Update rate benchmarks list
        entity.getRateBenchmarks().clear();
        for (RateBenchmark benchmarkDomain : domain.getRateBenchmarks()) {
            RateBenchmarkPersistenceEntity benchmarkEntity = new RateBenchmarkPersistenceEntity();
            benchmarkEntity.setId(benchmarkDomain.getId().value());
            benchmarkEntity.setRateType(benchmarkDomain.getRateType());
            benchmarkEntity.setAnnualRate(benchmarkDomain.getAnnualRate().value());
            benchmarkEntity.setCurrency(benchmarkDomain.getCurrency());
            benchmarkEntity.setSourceLabel(benchmarkDomain.getSourceLabel());
            benchmarkEntity.setSourceUrl(benchmarkDomain.getSourceUrl());
            benchmarkEntity.setEffectiveFrom(benchmarkDomain.getEffectiveFrom());
            entity.addRateBenchmark(benchmarkEntity);
        }

        return entity;
    }

    /**
     * Converts a JPA persistence entity to a FinancialEntity domain aggregate.
     *
     * @param entity The persistence entity.
     * @return The domain aggregate.
     */
    public static FinancialEntity toDomain(FinancialEntityPersistenceEntity entity) {
        List<RateBenchmark> benchmarks = entity.getRateBenchmarks().stream()
            .map(bEntity -> new RateBenchmark(
                new RateBenchmarkId(bEntity.getId()),
                bEntity.getRateType(),
                new Percent(bEntity.getAnnualRate()),
                bEntity.getCurrency(),
                bEntity.getSourceLabel(),
                bEntity.getSourceUrl(),
                bEntity.getEffectiveFrom()
            ))
            .collect(Collectors.toList());

        return new FinancialEntity(
            new FinancialEntityId(entity.getId()),
            entity.getName(),
            benchmarks
        );
    }
}

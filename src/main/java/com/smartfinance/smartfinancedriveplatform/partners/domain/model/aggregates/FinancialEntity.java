package com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.entities.RateBenchmark;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * FinancialEntity aggregate root.
 * Represents a partner banking / financial institution offering vehicle loans and rates.
 */
@Getter
public class FinancialEntity extends AbstractDomainAggregateRoot<FinancialEntity> {

    private final FinancialEntityId id;
    private String name;
    private final List<RateBenchmark> rateBenchmarks = new ArrayList<>();

    /**
     * Constructor for reconstituting from persistence.
     */
    public FinancialEntity(FinancialEntityId id, String name, List<RateBenchmark> rateBenchmarks) {
        this.id = id;
        this.name = name;
        if (rateBenchmarks != null) {
            this.rateBenchmarks.addAll(rateBenchmarks);
        }
    }

    /**
     * Constructor for creating a new FinancialEntity.
     */
    public FinancialEntity(String name) {
        this.id = new FinancialEntityId(UUID.randomUUID());
        setName(name);
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("partners.error.financialEntity.name.required");
        }
        this.name = name.trim();
    }

    public List<RateBenchmark> getRateBenchmarks() {
        return Collections.unmodifiableList(rateBenchmarks);
    }

    /**
     * Adds a rate benchmark to this financial entity.
     */
    public void addRateBenchmark(RateBenchmark benchmark) {
        if (benchmark == null) {
            throw new DomainValidationException("partners.error.rateBenchmark.required");
        }
        this.rateBenchmarks.add(benchmark);
    }
}

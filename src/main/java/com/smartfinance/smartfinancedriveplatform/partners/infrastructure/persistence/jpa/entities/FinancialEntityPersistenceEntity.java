package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing the 'financial_entities' table in the database.
 */
@Entity
@Table(name = "financial_entities")
@Getter
@Setter
@NoArgsConstructor
public class FinancialEntityPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "financialEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RateBenchmarkPersistenceEntity> rateBenchmarks = new ArrayList<>();

    public void addRateBenchmark(RateBenchmarkPersistenceEntity benchmark) {
        benchmark.setFinancialEntity(this);
        this.rateBenchmarks.add(benchmark);
    }
}

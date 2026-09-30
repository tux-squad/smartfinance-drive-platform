package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JPA entity representing the 'financial_entities' table in the database.
 */
@Entity
@Table(name = "financial_entities")
@Getter
@Setter
@NoArgsConstructor
public class FinancialEntityPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "ruc", length = 11, unique = true)
    private String ruc;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "banner_url", length = 1000)
    private String bannerUrl;

    @OneToMany(mappedBy = "financialEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RateBenchmarkPersistenceEntity> rateBenchmarks = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "financial_entity_allowed_domains",
            joinColumns = @JoinColumn(name = "financial_entity_id")
    )
    @Column(name = "domain", nullable = false)
    private Set<String> allowedDomains = new HashSet<>();

    public void addRateBenchmark(RateBenchmarkPersistenceEntity benchmark) {
        benchmark.setFinancialEntity(this);
        this.rateBenchmarks.add(benchmark);
    }
}

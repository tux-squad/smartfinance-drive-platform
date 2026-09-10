package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing the 'financial_entity_rate_benchmarks' table in the database.
 */
@Entity
@Table(name = "financial_entity_rate_benchmarks")
@Getter
@Setter
@NoArgsConstructor
public class RateBenchmarkPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_entity_id", nullable = false)
    private FinancialEntityPersistenceEntity financialEntity;

    @Column(name = "rate_type", nullable = false, length = 20)
    private String rateType;

    @Column(name = "annual_rate", nullable = false, precision = 9, scale = 6)
    private BigDecimal annualRate;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "source_label", nullable = false)
    private String sourceLabel;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
}

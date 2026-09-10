package com.smartfinance.smartfinancedriveplatform.partners.domain.model.entities;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.RateBenchmarkId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain entity representing an annual interest rate benchmark (e.g. TCEA/TEA) for a financial entity.
 */
@Getter
public class RateBenchmark {

    private final RateBenchmarkId id;
    private String rateType; // "TCEA", "TEA"
    private Percent annualRate;
    private String currency; // "PEN", "USD"
    private String sourceLabel;
    private String sourceUrl;
    private LocalDate effectiveFrom;

    /**
     * Constructor for reconstituting from persistence.
     */
    public RateBenchmark(RateBenchmarkId id, String rateType, Percent annualRate, 
                         String currency, String sourceLabel, String sourceUrl, 
                         LocalDate effectiveFrom) {
        this.id = id;
        this.rateType = rateType;
        this.annualRate = annualRate;
        this.currency = currency;
        this.sourceLabel = sourceLabel;
        this.sourceUrl = sourceUrl;
        this.effectiveFrom = effectiveFrom;
    }

    /**
     * Constructor for creating a new RateBenchmark.
     */
    public RateBenchmark(String rateType, Percent annualRate, String currency, 
                         String sourceLabel, String sourceUrl, LocalDate effectiveFrom) {
        this.id = new RateBenchmarkId(UUID.randomUUID());
        setRateType(rateType);
        setAnnualRate(annualRate);
        setCurrency(currency);
        setSourceLabel(sourceLabel);
        setSourceUrl(sourceUrl);
        setEffectiveFrom(effectiveFrom);
    }

    public void setRateType(String rateType) {
        if (rateType == null || rateType.isBlank()) {
            throw new DomainValidationException("partners.error.rateType.required");
        }
        this.rateType = rateType.trim().toUpperCase();
    }

    public void setAnnualRate(Percent annualRate) {
        if (annualRate == null) {
            throw new DomainValidationException("partners.error.annualRate.required");
        }
        this.annualRate = annualRate;
    }

    public void setCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new DomainValidationException("partners.error.currency.required");
        }
        this.currency = currency.trim().toUpperCase();
    }

    public void setSourceLabel(String sourceLabel) {
        if (sourceLabel == null || sourceLabel.isBlank()) {
            throw new DomainValidationException("partners.error.sourceLabel.required");
        }
        this.sourceLabel = sourceLabel.trim();
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl != null ? sourceUrl.trim() : null;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        if (effectiveFrom == null) {
            throw new DomainValidationException("partners.error.effectiveFrom.required");
        }
        this.effectiveFrom = effectiveFrom;
    }
}

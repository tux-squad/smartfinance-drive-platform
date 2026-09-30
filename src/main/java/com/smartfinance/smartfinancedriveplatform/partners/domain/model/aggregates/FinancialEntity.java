package com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.entities.RateBenchmark;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * FinancialEntity aggregate root.
 * Represents a partner banking / financial institution offering vehicle loans and rates.
 */
@Getter
public class FinancialEntity extends AbstractDomainAggregateRoot<FinancialEntity> {

    private final FinancialEntityId id;
    private String userId;
    private String ruc;
    private String name;
    private String logoUrl;
    private String bannerUrl;
    private final List<RateBenchmark> rateBenchmarks = new ArrayList<>();
    private final Set<String> allowedDomains = new HashSet<>();

    /**
     * Constructor for reconstituting from persistence with userId, ruc, and allowedDomains.
     */
    public FinancialEntity(FinancialEntityId id, String userId, String ruc, String name, String logoUrl, String bannerUrl, List<RateBenchmark> rateBenchmarks, Collection<String> allowedDomains) {
        this(id, userId, ruc, name, logoUrl, bannerUrl, rateBenchmarks);
        if (allowedDomains != null) {
            allowedDomains.forEach(this::addAllowedDomain);
        }
    }

    /**
     * Constructor for reconstituting from persistence with userId and ruc.
     */
    public FinancialEntity(FinancialEntityId id, String userId, String ruc, String name, String logoUrl, String bannerUrl, List<RateBenchmark> rateBenchmarks) {
        this.id = id;
        this.userId = userId;
        setRuc(ruc);
        this.name = name;
        setLogoUrl(logoUrl);
        setBannerUrl(bannerUrl);
        if (rateBenchmarks != null) {
            this.rateBenchmarks.addAll(rateBenchmarks);
        }
    }

    public FinancialEntity(FinancialEntityId id, String userId, String name, String logoUrl, String bannerUrl, List<RateBenchmark> rateBenchmarks) {
        this(id, userId, null, name, logoUrl, bannerUrl, rateBenchmarks);
    }

    public FinancialEntity(FinancialEntityId id, String name, String logoUrl, String bannerUrl, List<RateBenchmark> rateBenchmarks) {
        this(id, null, null, name, logoUrl, bannerUrl, rateBenchmarks);
    }

    public FinancialEntity(FinancialEntityId id, String name, List<RateBenchmark> rateBenchmarks) {
        this(id, null, null, name, null, null, rateBenchmarks);
    }

    /**
     * Constructor for creating a new FinancialEntity.
     */
    public FinancialEntity(String name, String logoUrl, String bannerUrl) {
        this.id = new FinancialEntityId(UUID.randomUUID());
        this.userId = null;
        this.ruc = null;
        setName(name);
        setLogoUrl(logoUrl);
        setBannerUrl(bannerUrl);
    }

    public FinancialEntity(String name) {
        this(name, null, null);
    }

    public String getUserId() {
        return userId;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        if (ruc != null && !ruc.isBlank()) {
            String trimmed = ruc.trim();
            if (!trimmed.matches("^\\d{11}$")) {
                throw new DomainValidationException("partners.error.financialEntity.ruc.invalid");
            }
            this.ruc = trimmed;
        } else {
            this.ruc = null;
        }
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("partners.error.financialEntity.name.required");
        }
        this.name = name.trim();
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = (logoUrl != null && !logoUrl.isBlank()) ? logoUrl.trim() : null;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = (bannerUrl != null && !bannerUrl.isBlank()) ? bannerUrl.trim() : null;
    }

    public void setUserId(String userId) {
        this.userId = (userId != null && !userId.isBlank()) ? userId.trim() : null;
    }

    public void updateDetails(String name, String logoUrl, String bannerUrl) {
        updateDetails(name, logoUrl, bannerUrl, this.userId, this.ruc);
    }

    public void updateDetails(String name, String logoUrl, String bannerUrl, String userId) {
        updateDetails(name, logoUrl, bannerUrl, userId, this.ruc);
    }

    public void updateDetails(String name, String logoUrl, String bannerUrl, String userId, String ruc) {
        setName(name);
        if (logoUrl != null) {
            setLogoUrl(logoUrl);
        }
        if (bannerUrl != null) {
            setBannerUrl(bannerUrl);
        }
        if (userId != null) {
            setUserId(userId);
        }
        if (ruc != null) {
            setRuc(ruc);
        }
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

    public Set<String> getAllowedDomains() {
        return Collections.unmodifiableSet(allowedDomains);
    }

    public void addAllowedDomain(String domain) {
        if (domain != null && !domain.isBlank()) {
            String normalized = domain.trim().toLowerCase();
            if (normalized.startsWith("@")) {
                normalized = normalized.substring(1);
            }
            this.allowedDomains.add(normalized);
        }
    }

    public boolean isDomainAllowed(String domainOrEmail) {
        if (domainOrEmail == null || domainOrEmail.isBlank()) {
            return false;
        }
        String domain = domainOrEmail.trim().toLowerCase();
        if (domain.contains("@")) {
            domain = domain.substring(domain.lastIndexOf("@") + 1);
        }
        return this.allowedDomains.contains(domain);
    }
}

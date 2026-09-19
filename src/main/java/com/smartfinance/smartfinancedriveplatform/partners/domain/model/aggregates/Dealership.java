package com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.UUID;

/**
 * Dealership Aggregate Root.
 * Represents a B2B partner dealership registered in the platform.
 */
@Getter
public class Dealership extends AbstractDomainAggregateRoot<Dealership> {

    private final DealershipId id;
    private String userId;
    private String ruc;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String description;
    private String operatingHours;
    private Double rating;
    private String logoUrl;
    private String bannerUrl;
    private boolean active;

    /**
     * Constructor for reconstituting from persistence.
     */
    public Dealership(DealershipId id, String userId, String ruc, String name, String address, 
                      String phone, String email, String website, String description, 
                      String operatingHours, Double rating, String logoUrl, String bannerUrl, 
                      boolean active) {
        this.id = id;
        this.userId = userId;
        setRuc(ruc);
        setName(name);
        setAddress(address);
        this.phone = phone;
        this.email = email;
        this.website = website;
        this.description = description;
        this.operatingHours = operatingHours;
        this.rating = rating != null ? rating : 5.0;
        this.logoUrl = logoUrl;
        this.bannerUrl = bannerUrl;
        this.active = active;
    }

    /**
     * Constructor for creating a new Dealership aggregate.
     */
    public Dealership(String userId, String ruc, String name, String address, 
                      String phone, String email, String website, String description, 
                      String operatingHours, String logoUrl, String bannerUrl) {
        this.id = new DealershipId(UUID.randomUUID());
        setUserId(userId);
        setRuc(ruc);
        setName(name);
        setAddress(address);
        this.phone = phone != null ? phone.trim() : null;
        this.email = email != null ? email.trim() : null;
        this.website = website != null ? website.trim() : null;
        this.description = description != null ? description.trim() : null;
        this.operatingHours = operatingHours != null ? operatingHours.trim() : null;
        this.rating = 5.0;
        this.logoUrl = logoUrl;
        this.bannerUrl = bannerUrl;
        this.active = true;
    }

    public void setUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new DomainValidationException("partners.error.dealership.userId.required");
        }
        this.userId = userId.trim();
    }

    public void setRuc(String ruc) {
        if (ruc == null || ruc.isBlank() || !ruc.matches("\\d{11}")) {
            throw new DomainValidationException("partners.error.dealership.ruc.invalid");
        }
        this.ruc = ruc.trim();
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("partners.error.dealership.name.required");
        }
        this.name = name.trim();
    }

    public void setAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new DomainValidationException("partners.error.dealership.address.required");
        }
        this.address = address.trim();
    }

    public void setRating(Double rating) {
        if (rating != null && (rating < 0.0 || rating > 5.0)) {
            throw new DomainValidationException("partners.error.dealership.rating.invalid");
        }
        this.rating = rating != null ? rating : 5.0;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl != null ? logoUrl.trim() : null;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl != null ? bannerUrl.trim() : null;
    }

    public void updateDetails(String ruc, String name, String address, String phone, 
                              String email, String website, String description, String operatingHours) {
        setRuc(ruc);
        setName(name);
        setAddress(address);
        this.phone = phone != null ? phone.trim() : null;
        this.email = email != null ? email.trim() : null;
        this.website = website != null ? website.trim() : null;
        this.description = description != null ? description.trim() : null;
        this.operatingHours = operatingHours != null ? operatingHours.trim() : null;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}

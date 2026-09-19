package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities.DealershipPersistenceEntity;

/**
 * Assembler to convert between Dealership domain model and JPA entity.
 */
public final class DealershipPersistenceAssembler {

    private DealershipPersistenceAssembler() {}

    public static DealershipPersistenceEntity toEntity(Dealership domain, DealershipPersistenceEntity entity) {
        if (entity == null) {
            entity = new DealershipPersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setUserId(domain.getUserId());
        entity.setRuc(domain.getRuc());
        entity.setName(domain.getName());
        entity.setAddress(domain.getAddress());
        entity.setPhone(domain.getPhone());
        entity.setEmail(domain.getEmail());
        entity.setWebsite(domain.getWebsite());
        entity.setDescription(domain.getDescription());
        entity.setOperatingHours(domain.getOperatingHours());
        entity.setRating(domain.getRating());
        entity.setLogoUrl(domain.getLogoUrl());
        entity.setBannerUrl(domain.getBannerUrl());
        entity.setActive(domain.isActive());
        return entity;
    }

    public static Dealership toDomain(DealershipPersistenceEntity entity) {
        return new Dealership(
            new DealershipId(entity.getId()),
            entity.getUserId(),
            entity.getRuc(),
            entity.getName(),
            entity.getAddress(),
            entity.getPhone(),
            entity.getEmail(),
            entity.getWebsite(),
            entity.getDescription(),
            entity.getOperatingHours(),
            entity.getRating(),
            entity.getLogoUrl(),
            entity.getBannerUrl(),
            entity.isActive()
        );
    }
}

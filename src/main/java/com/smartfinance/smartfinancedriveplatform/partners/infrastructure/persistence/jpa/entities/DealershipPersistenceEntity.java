package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing 'dealerships' table in the database.
 */
@Entity
@Table(name = "dealerships", indexes = {
        @Index(name = "idx_dealerships_user_id", columnList = "user_id"),
        @Index(name = "idx_dealerships_ruc", columnList = "ruc")
})
@Getter
@Setter
@NoArgsConstructor
public class DealershipPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "ruc", nullable = false, length = 11)
    private String ruc;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "operating_hours", length = 255)
    private String operatingHours;

    @Column(name = "rating")
    private Double rating = 5.0;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "banner_url", length = 1000)
    private String bannerUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}

package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity representing the 'vehicles' table in the database.
 */
@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicles_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class VehiclePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "financial_entity_id", nullable = false)
    private UUID financialEntityId;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "manufacture_year", nullable = false)
    private int manufactureYear;

    @Column(name = "condition", nullable = false, length = 20)
    private String condition;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "mileage")
    private Integer mileage;

    @Column(name = "transmission", length = 50)
    private String transmission;

    @Column(name = "engine", length = 50)
    private String engine;

    @Column(name = "traction", length = 50)
    private String traction;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vehicle_images", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
}


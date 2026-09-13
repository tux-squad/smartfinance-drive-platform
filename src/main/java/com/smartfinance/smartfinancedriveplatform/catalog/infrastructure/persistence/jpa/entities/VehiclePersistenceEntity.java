package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity representing the 'vehicles' table in the database.
 */
@Entity
@Table(name = "vehicles")
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
}

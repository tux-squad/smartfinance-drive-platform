package com.smartfinance.smartfinancedriveplatform.projections.infrastructure.persistence.jpa.entities;

import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity representing the 'depreciation_projections' table in the database.
 */
@Entity
@Table(name = "depreciation_projections")
@Getter
@Setter
@NoArgsConstructor
public class DepreciationProjectionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "simulation_id")
    private String simulationId;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "initial_vehicle_price_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal initialVehiclePriceAmount;

    @Column(name = "manufacture_year", nullable = false)
    private int manufactureYear;

    @Column(name = "motorization_type", nullable = false, length = 20)
    private String motorizationType;

    @Column(name = "annual_depreciation_rate", nullable = false, precision = 9, scale = 6)
    private BigDecimal annualDepreciationRate;

    @Column(name = "projected_value_2_years_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal projectedValue2YearsAmount;

    @Column(name = "projected_value_3_years_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal projectedValue3YearsAmount;

    @Column(name = "projected_value_5_years_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal projectedValue5YearsAmount;

    @Column(name = "balloon_payment_amount", precision = 12, scale = 2)
    private BigDecimal balloonPaymentAmount;

    @Column(name = "recommended_action", nullable = false, length = 30)
    private String recommendedAction;

    @Column(name = "advisory_notes", length = 500)
    private String advisoryNotes;
}

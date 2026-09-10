package com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.MotorizationType;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.ProjectionId;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.RecommendedAction;
import com.smartfinance.smartfinancedriveplatform.projections.domain.services.DepreciationCalculator;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;
import lombok.Getter;

import java.util.UUID;

/**
 * DepreciationProjection aggregate root representing a vehicle market value projection over time.
 */
@Getter
public class DepreciationProjection extends AbstractDomainAggregateRoot<DepreciationProjection> {

    private final ProjectionId id;
    private String vehicleId;
    private String simulationId;
    private Money initialVehiclePrice;
    private int manufactureYear;
    private MotorizationType motorizationType;
    private Percent annualDepreciationRate;
    private Money projectedValue2Years;
    private Money projectedValue3Years;
    private Money projectedValue5Years;
    private Money balloonPaymentAmount;
    private RecommendedAction recommendedAction;
    private String advisoryNotes;

    /**
     * Reconstitution constructor from persistence.
     */
    public DepreciationProjection(ProjectionId id, String vehicleId, String simulationId, Money initialVehiclePrice,
                                  int manufactureYear, MotorizationType motorizationType, Percent annualDepreciationRate,
                                  Money projectedValue2Years, Money projectedValue3Years, Money projectedValue5Years,
                                  Money balloonPaymentAmount, RecommendedAction recommendedAction, String advisoryNotes) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.simulationId = simulationId;
        this.initialVehiclePrice = initialVehiclePrice;
        this.manufactureYear = manufactureYear;
        this.motorizationType = motorizationType;
        this.annualDepreciationRate = annualDepreciationRate;
        this.projectedValue2Years = projectedValue2Years;
        this.projectedValue3Years = projectedValue3Years;
        this.projectedValue5Years = projectedValue5Years;
        this.balloonPaymentAmount = balloonPaymentAmount;
        this.recommendedAction = recommendedAction;
        this.advisoryNotes = advisoryNotes;
    }

    /**
     * Domain Constructor for creating and computing a new vehicle depreciation projection.
     */
    public DepreciationProjection(String vehicleId, String simulationId, Money initialVehiclePrice,
                                  int manufactureYear, MotorizationType motorizationType, Money balloonPaymentAmount) {
        this.id = new ProjectionId(UUID.randomUUID());
        setVehicleId(vehicleId);
        this.simulationId = simulationId;
        this.initialVehiclePrice = initialVehiclePrice;
        setManufactureYear(manufactureYear);
        this.motorizationType = motorizationType != null ? motorizationType : MotorizationType.COMBUSTION;
        this.balloonPaymentAmount = balloonPaymentAmount;

        // Calculate projection curve and recommendations
        calculateProjection();
    }

    public void setVehicleId(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank()) {
            throw new DomainValidationException("projections.error.vehicleId.required");
        }
        this.vehicleId = vehicleId.trim();
    }

    public void setManufactureYear(int manufactureYear) {
        if (manufactureYear < 1900) {
            throw new DomainValidationException("projections.error.manufactureYear.invalid");
        }
        this.manufactureYear = manufactureYear;
    }

    public void calculateProjection() {
        DepreciationCalculator calculator = new DepreciationCalculator();
        DepreciationCalculator.CalculationOutput output = calculator.calculateProjection(
                this.initialVehiclePrice,
                this.motorizationType,
                this.balloonPaymentAmount
        );

        this.projectedValue2Years = output.projectedValue2Years();
        this.projectedValue3Years = output.projectedValue3Years();
        this.projectedValue5Years = output.projectedValue5Years();
        this.annualDepreciationRate = output.annualDepreciationRate();
        this.recommendedAction = output.recommendedAction();
        this.advisoryNotes = output.advisoryNotes();
    }
}

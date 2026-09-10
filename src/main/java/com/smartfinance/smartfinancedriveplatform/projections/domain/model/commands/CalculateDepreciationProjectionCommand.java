package com.smartfinance.smartfinancedriveplatform.projections.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.MotorizationType;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

/**
 * Command to calculate a new depreciation projection for a vehicle.
 */
public record CalculateDepreciationProjectionCommand(
        String vehicleId,
        String simulationId,
        Money initialVehiclePrice,
        int manufactureYear,
        MotorizationType motorizationType,
        Money balloonPaymentAmount
) {}

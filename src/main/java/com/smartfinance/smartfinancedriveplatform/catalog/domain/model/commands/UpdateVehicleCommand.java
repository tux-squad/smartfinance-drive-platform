package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

/**
 * Command to request update details of an existing vehicle.
 */
public record UpdateVehicleCommand(
    VehicleId vehicleId,
    FinancialEntityId financialEntityId,
    String brand,
    String model,
    int manufactureYear,
    String condition,
    Money price,
    String imagePath
) {}

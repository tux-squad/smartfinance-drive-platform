package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import java.util.List;

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
    String imagePath,
    String status,
    Integer mileage,
    String transmission,
    String engine,
    String traction,
    List<String> images
) {
    public UpdateVehicleCommand(VehicleId vehicleId, FinancialEntityId financialEntityId, String brand, String model, int manufactureYear, String condition, Money price, String imagePath) {
        this(vehicleId, financialEntityId, brand, model, manufactureYear, condition, price, imagePath, "ACTIVE", 0, null, null, null, null);
    }
}


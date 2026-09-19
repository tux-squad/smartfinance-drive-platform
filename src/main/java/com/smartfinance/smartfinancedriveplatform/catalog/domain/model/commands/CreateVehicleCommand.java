package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import java.util.List;

/**
 * Command to request the registration of a new vehicle in the catalog.
 */
public record CreateVehicleCommand(
    UserId userId,
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
    public CreateVehicleCommand(UserId userId, FinancialEntityId financialEntityId, String brand, String model, int manufactureYear, String condition, Money price, String imagePath) {
        this(userId, financialEntityId, brand, model, manufactureYear, condition, price, imagePath, "ACTIVE", 0, null, null, null, null);
    }
}


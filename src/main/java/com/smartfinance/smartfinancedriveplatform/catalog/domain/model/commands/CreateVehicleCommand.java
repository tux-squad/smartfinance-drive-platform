package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

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
    String imagePath
) {}

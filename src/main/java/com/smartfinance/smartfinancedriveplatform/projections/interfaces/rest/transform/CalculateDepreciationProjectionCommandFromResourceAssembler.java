package com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.commands.CalculateDepreciationProjectionCommand;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.MotorizationType;
import com.smartfinance.smartfinancedriveplatform.projections.interfaces.rest.resources.CalculateDepreciationProjectionResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

/**
 * Assembler class to transform CalculateDepreciationProjectionResource into CalculateDepreciationProjectionCommand.
 */
public final class CalculateDepreciationProjectionCommandFromResourceAssembler {

    private CalculateDepreciationProjectionCommandFromResourceAssembler() {}

    public static CalculateDepreciationProjectionCommand toCommandFromResource(CalculateDepreciationProjectionResource resource) {
        String currency = (resource.currency() != null && !resource.currency().isBlank()) ? resource.currency() : Money.DEFAULT_CURRENCY;
        Money price = resource.initialVehiclePriceAmount() != null ? new Money(resource.initialVehiclePriceAmount(), currency) : Money.zero(currency);
        Money balloon = resource.balloonPaymentAmount() != null ? new Money(resource.balloonPaymentAmount(), currency) : Money.zero(currency);
        MotorizationType motorization = resource.motorizationType() != null ? MotorizationType.valueOf(resource.motorizationType().toUpperCase()) : MotorizationType.COMBUSTION;

        return new CalculateDepreciationProjectionCommand(
                resource.vehicleId(),
                resource.simulationId(),
                price,
                resource.manufactureYear(),
                motorization,
                balloon
        );
    }
}

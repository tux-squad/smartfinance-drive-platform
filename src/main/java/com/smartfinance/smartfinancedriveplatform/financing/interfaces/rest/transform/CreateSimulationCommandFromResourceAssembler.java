package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.CreateSimulationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.VehicleInsuranceType;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.CreateSimulationResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

/**
 * Assembler class to transform a CreateSimulationResource DTO into a CreateSimulationCommand.
 */
public final class CreateSimulationCommandFromResourceAssembler {

    private CreateSimulationCommandFromResourceAssembler() {}

    public static CreateSimulationCommand toCommandFromResource(CreateSimulationResource resource) {
        return toCommandFromResource(resource, null);
    }

    public static CreateSimulationCommand toCommandFromResource(CreateSimulationResource resource, String authenticatedUserId) {
        String currency = (resource.currency() != null && !resource.currency().isBlank()) ? resource.currency() : Money.DEFAULT_CURRENCY;
        Money price = resource.vehiclePriceAmount() != null ? new Money(resource.vehiclePriceAmount(), currency) : Money.zero(currency);
        Percent downPayment = resource.downPaymentPercentage() != null ? new Percent(resource.downPaymentPercentage()) : Percent.of(20.0);
        Percent balloon = resource.balloonPaymentPercentage() != null ? new Percent(resource.balloonPaymentPercentage()) : Percent.of(0.0);
        Percent tea = resource.annualEffectiveRate() != null ? new Percent(resource.annualEffectiveRate()) : Percent.of(12.0);
        Percent desgravamen = resource.monthlyCreditLifeInsuranceRate() != null ? new Percent(resource.monthlyCreditLifeInsuranceRate()) : Percent.of(0.05);
        Money vehicleIns = resource.vehicleInsuranceFeeAmount() != null ? new Money(resource.vehicleInsuranceFeeAmount(), currency) : Money.zero(currency);
        VehicleInsuranceType vehicleInsType = resource.vehicleInsuranceType() != null ? VehicleInsuranceType.valueOf(resource.vehicleInsuranceType().toUpperCase()) : VehicleInsuranceType.ENDOSADO;
        GracePeriodType graceType = resource.gracePeriodType() != null ? GracePeriodType.valueOf(resource.gracePeriodType().toUpperCase()) : GracePeriodType.NONE;
        Money initialFees = resource.initialFeesAmount() != null ? new Money(resource.initialFeesAmount(), currency) : Money.zero(currency);
        Percent discountRate = resource.discountRate() != null ? new Percent(resource.discountRate()) : Percent.of(10.0);

        String targetUserId = (authenticatedUserId != null && !authenticatedUserId.isBlank())
                ? authenticatedUserId
                : resource.userId();

        return new CreateSimulationCommand(
                resource.title(),
                targetUserId,
                resource.vehicleId(),
                resource.financialEntityId(),
                price,
                downPayment,
                balloon,
                tea,
                desgravamen,
                vehicleIns,
                vehicleInsType,
                resource.loanTermMonths(),
                graceType,
                resource.gracePeriodMonths(),
                initialFees,
                discountRate,
                resource.startDate()
        );
    }
}

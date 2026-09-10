package com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.VehicleInsuranceType;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

import java.time.LocalDate;

/**
 * Command to create and compute a new credit simulation.
 */
public record CreateSimulationCommand(
        String title,
        String userId,
        String vehicleId,
        String financialEntityId,
        Money vehiclePrice,
        Percent downPaymentPercentage,
        Percent balloonPaymentPercentage,
        Percent annualEffectiveRate,
        Percent monthlyCreditLifeInsuranceRate,
        Money vehicleInsuranceFee,
        VehicleInsuranceType vehicleInsuranceType,
        int loanTermMonths,
        GracePeriodType gracePeriodType,
        int gracePeriodMonths,
        Money initialFees,
        Percent discountRate,
        LocalDate startDate
) {}

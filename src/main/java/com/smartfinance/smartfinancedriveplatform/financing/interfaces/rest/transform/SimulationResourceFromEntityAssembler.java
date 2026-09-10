package com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.entities.PaymentPeriod;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.FinancialMetricsResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.PaymentPeriodResource;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.resources.SimulationResource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Assembler class to transform a Simulation domain aggregate into a SimulationResource DTO.
 */
public final class SimulationResourceFromEntityAssembler {

    private SimulationResourceFromEntityAssembler() {}

    public static SimulationResource toResourceFromEntity(Simulation entity) {
        FinancialMetricsResource metrics = new FinancialMetricsResource(
                entity.getFinancedAmount().amount(),
                entity.getDownPaymentAmount().amount(),
                entity.getBalloonPaymentAmount().amount(),
                entity.getTcea().value(),
                entity.getTir().value(),
                entity.getVan().amount(),
                entity.getTotalInterest().amount(),
                entity.getTotalAmount().amount()
        );

        List<PaymentPeriodResource> schedule = entity.getPaymentPeriods().stream()
                .map(SimulationResourceFromEntityAssembler::toPeriodResource)
                .collect(Collectors.toList());

        return new SimulationResource(
                entity.getId().value(),
                entity.getTitle(),
                entity.getUserId(),
                entity.getVehicleId(),
                entity.getFinancialEntityId(),
                entity.getVehiclePrice().currency(),
                entity.getVehiclePrice().amount(),
                entity.getDownPaymentPercentage().value(),
                entity.getBalloonPaymentPercentage().value(),
                entity.getAnnualEffectiveRate().value(),
                entity.getMonthlyCreditLifeInsuranceRate() != null ? entity.getMonthlyCreditLifeInsuranceRate().value() : null,
                entity.getVehicleInsuranceFee() != null ? entity.getVehicleInsuranceFee().amount() : null,
                entity.getVehicleInsuranceType().name(),
                entity.getLoanTermMonths(),
                entity.getGracePeriodType().name(),
                entity.getGracePeriodMonths(),
                entity.getInitialFees() != null ? entity.getInitialFees().amount() : null,
                entity.getDiscountRate() != null ? entity.getDiscountRate().value() : null,
                entity.getStartDate(),
                metrics,
                schedule
        );
    }

    private static PaymentPeriodResource toPeriodResource(PaymentPeriod period) {
        return new PaymentPeriodResource(
                period.getId().value(),
                period.getPeriodNumber(),
                period.getDueDate(),
                period.getDaysInPeriod(),
                period.getInitialBalance().currency(),
                period.getInitialBalance().amount(),
                period.getInterestPayment().amount(),
                period.getPrincipalAmortization().amount(),
                period.getCreditLifeInsurance().amount(),
                period.getVehicleInsurance().amount(),
                period.getTotalInstallment().amount(),
                period.getFinalBalance().amount(),
                period.getGraceType().name()
        );
    }
}

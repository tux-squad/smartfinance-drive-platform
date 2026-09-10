package com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.assemblers;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.entities.PaymentPeriod;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.PaymentPeriodId;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.VehicleInsuranceType;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities.PaymentPeriodPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities.SimulationPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Assembler class to convert between Simulation domain aggregate and JPA persistence entities.
 */
public final class SimulationPersistenceAssembler {

    private SimulationPersistenceAssembler() {}

    public static SimulationPersistenceEntity toEntity(Simulation domain, SimulationPersistenceEntity entity) {
        if (entity == null) {
            entity = new SimulationPersistenceEntity();
        }
        entity.setId(domain.getId().value());
        entity.setTitle(domain.getTitle());
        entity.setUserId(domain.getUserId());
        entity.setVehicleId(domain.getVehicleId());
        entity.setFinancialEntityId(domain.getFinancialEntityId());
        entity.setCurrency(domain.getVehiclePrice().currency());
        entity.setVehiclePriceAmount(domain.getVehiclePrice().amount());
        entity.setDownPaymentPercentage(domain.getDownPaymentPercentage().value());
        entity.setBalloonPaymentPercentage(domain.getBalloonPaymentPercentage().value());
        entity.setAnnualEffectiveRate(domain.getAnnualEffectiveRate().value());
        entity.setMonthlyCreditLifeInsuranceRate(domain.getMonthlyCreditLifeInsuranceRate() != null ? domain.getMonthlyCreditLifeInsuranceRate().value() : null);
        entity.setVehicleInsuranceFeeAmount(domain.getVehicleInsuranceFee() != null ? domain.getVehicleInsuranceFee().amount() : null);
        entity.setVehicleInsuranceType(domain.getVehicleInsuranceType().name());
        entity.setLoanTermMonths(domain.getLoanTermMonths());
        entity.setGracePeriodType(domain.getGracePeriodType().name());
        entity.setGracePeriodMonths(domain.getGracePeriodMonths());
        entity.setInitialFeesAmount(domain.getInitialFees() != null ? domain.getInitialFees().amount() : null);
        entity.setDiscountRate(domain.getDiscountRate() != null ? domain.getDiscountRate().value() : null);
        entity.setStartDate(domain.getStartDate());

        entity.setFinancedAmount(domain.getFinancedAmount().amount());
        entity.setDownPaymentAmount(domain.getDownPaymentAmount().amount());
        entity.setBalloonPaymentAmount(domain.getBalloonPaymentAmount().amount());
        entity.setTcea(domain.getTcea().value());
        entity.setTir(domain.getTir().value());
        entity.setVan(domain.getVan().amount());
        entity.setTotalInterest(domain.getTotalInterest().amount());
        entity.setTotalAmount(domain.getTotalAmount().amount());

        entity.getPaymentPeriods().clear();
        for (PaymentPeriod periodDomain : domain.getPaymentPeriods()) {
            PaymentPeriodPersistenceEntity periodEntity = new PaymentPeriodPersistenceEntity();
            periodEntity.setId(periodDomain.getId().value());
            periodEntity.setPeriodNumber(periodDomain.getPeriodNumber());
            periodEntity.setDueDate(periodDomain.getDueDate());
            periodEntity.setDaysInPeriod(periodDomain.getDaysInPeriod());
            periodEntity.setCurrency(domain.getVehiclePrice().currency());
            periodEntity.setInitialBalanceAmount(periodDomain.getInitialBalance().amount());
            periodEntity.setInterestPaymentAmount(periodDomain.getInterestPayment().amount());
            periodEntity.setPrincipalAmortizationAmount(periodDomain.getPrincipalAmortization().amount());
            periodEntity.setCreditLifeInsuranceAmount(periodDomain.getCreditLifeInsurance().amount());
            periodEntity.setVehicleInsuranceAmount(periodDomain.getVehicleInsurance().amount());
            periodEntity.setTotalInstallmentAmount(periodDomain.getTotalInstallment().amount());
            periodEntity.setFinalBalanceAmount(periodDomain.getFinalBalance().amount());
            periodEntity.setGraceType(periodDomain.getGraceType().name());
            entity.addPaymentPeriod(periodEntity);
        }

        return entity;
    }

    public static Simulation toDomain(SimulationPersistenceEntity entity) {
        String currency = entity.getCurrency();

        List<PaymentPeriod> periods = entity.getPaymentPeriods().stream()
                .map(pEntity -> new PaymentPeriod(
                        new PaymentPeriodId(pEntity.getId()),
                        pEntity.getPeriodNumber(),
                        pEntity.getDueDate(),
                        pEntity.getDaysInPeriod(),
                        new Money(pEntity.getInitialBalanceAmount(), currency),
                        new Money(pEntity.getInterestPaymentAmount(), currency),
                        new Money(pEntity.getPrincipalAmortizationAmount(), currency),
                        new Money(pEntity.getCreditLifeInsuranceAmount(), currency),
                        new Money(pEntity.getVehicleInsuranceAmount(), currency),
                        new Money(pEntity.getTotalInstallmentAmount(), currency),
                        new Money(pEntity.getFinalBalanceAmount(), currency),
                        GracePeriodType.valueOf(pEntity.getGraceType())
                ))
                .collect(Collectors.toList());

        return new Simulation(
                new SimulationId(entity.getId()),
                entity.getTitle(),
                entity.getUserId(),
                entity.getVehicleId(),
                entity.getFinancialEntityId(),
                new Money(entity.getVehiclePriceAmount(), currency),
                new Percent(entity.getDownPaymentPercentage()),
                new Percent(entity.getBalloonPaymentPercentage()),
                new Percent(entity.getAnnualEffectiveRate()),
                entity.getMonthlyCreditLifeInsuranceRate() != null ? new Percent(entity.getMonthlyCreditLifeInsuranceRate()) : null,
                entity.getVehicleInsuranceFeeAmount() != null ? new Money(entity.getVehicleInsuranceFeeAmount(), currency) : null,
                VehicleInsuranceType.valueOf(entity.getVehicleInsuranceType()),
                entity.getLoanTermMonths(),
                GracePeriodType.valueOf(entity.getGracePeriodType()),
                entity.getGracePeriodMonths(),
                entity.getInitialFeesAmount() != null ? new Money(entity.getInitialFeesAmount(), currency) : null,
                entity.getDiscountRate() != null ? new Percent(entity.getDiscountRate()) : null,
                entity.getStartDate(),
                new Money(entity.getFinancedAmount(), currency),
                new Money(entity.getDownPaymentAmount(), currency),
                new Money(entity.getBalloonPaymentAmount(), currency),
                new Percent(entity.getTcea()),
                new Percent(entity.getTir()),
                new Money(entity.getVan(), currency),
                new Money(entity.getTotalInterest(), currency),
                new Money(entity.getTotalAmount(), currency),
                periods
        );
    }
}

package com.smartfinance.smartfinancedriveplatform.financing.domain.model.entities;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.PaymentPeriodId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an individual payment period in a credit simulation's payment schedule.
 */
public class PaymentPeriod {
    private final PaymentPeriodId id;
    private final int periodNumber;
    private final LocalDate dueDate;
    private final int daysInPeriod;
    private final Money initialBalance;
    private final Money interestPayment;
    private final Money principalAmortization;
    private final Money creditLifeInsurance;
    private final Money vehicleInsurance;
    private final Money totalInstallment;
    private final Money finalBalance;
    private final GracePeriodType graceType;

    public PaymentPeriod(PaymentPeriodId id, int periodNumber, LocalDate dueDate, int daysInPeriod,
                         Money initialBalance, Money interestPayment, Money principalAmortization,
                         Money creditLifeInsurance, Money vehicleInsurance, Money totalInstallment,
                         Money finalBalance, GracePeriodType graceType) {
        this.id = id != null ? id : new PaymentPeriodId(UUID.randomUUID());
        this.periodNumber = periodNumber;
        this.dueDate = dueDate;
        this.daysInPeriod = daysInPeriod;
        this.initialBalance = initialBalance;
        this.interestPayment = interestPayment;
        this.principalAmortization = principalAmortization;
        this.creditLifeInsurance = creditLifeInsurance;
        this.vehicleInsurance = vehicleInsurance;
        this.totalInstallment = totalInstallment;
        this.finalBalance = finalBalance;
        this.graceType = graceType != null ? graceType : GracePeriodType.NONE;
    }

    public PaymentPeriod(int periodNumber, LocalDate dueDate, int daysInPeriod,
                         Money initialBalance, Money interestPayment, Money principalAmortization,
                         Money creditLifeInsurance, Money vehicleInsurance, Money totalInstallment,
                         Money finalBalance, GracePeriodType graceType) {
        this(new PaymentPeriodId(UUID.randomUUID()), periodNumber, dueDate, daysInPeriod, initialBalance,
                interestPayment, principalAmortization, creditLifeInsurance, vehicleInsurance, totalInstallment,
                finalBalance, graceType);
    }

    public PaymentPeriodId getId() {
        return id;
    }

    public int getPeriodNumber() {
        return periodNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public int getDaysInPeriod() {
        return daysInPeriod;
    }

    public Money getInitialBalance() {
        return initialBalance;
    }

    public Money getInterestPayment() {
        return interestPayment;
    }

    public Money getPrincipalAmortization() {
        return principalAmortization;
    }

    public Money getCreditLifeInsurance() {
        return creditLifeInsurance;
    }

    public Money getVehicleInsurance() {
        return vehicleInsurance;
    }

    public Money getTotalInstallment() {
        return totalInstallment;
    }

    public Money getFinalBalance() {
        return finalBalance;
    }

    public GracePeriodType getGraceType() {
        return graceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentPeriod that = (PaymentPeriod) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

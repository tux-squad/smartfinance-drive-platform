package com.smartfinance.smartfinancedriveplatform.financing.domain.services;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.entities.PaymentPeriod;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.VehicleInsuranceType;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain Service responsible for building credit simulation payment schedules
 * and computing financial indicators (TIR, TCEA, VAN) in compliance with SBS guidelines.
 */
public class FinancingPlanBuilder {

    public record CalculationOutput(
            List<PaymentPeriod> periods,
            Money financedAmount,
            Money downPaymentAmount,
            Money balloonPaymentAmount,
            Percent tcea,
            Percent tir,
            Money van,
            Money totalInterest,
            Money totalAmount
    ) {}

    /**
     * Build financing plan and compute all financial metrics.
     */
    public CalculationOutput buildPlan(
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
    ) {
        validateInputs(vehiclePrice, downPaymentPercentage, balloonPaymentPercentage, annualEffectiveRate,
                loanTermMonths, gracePeriodMonths, gracePeriodType);

        String currency = vehiclePrice.currency();
        LocalDate baseDate = startDate != null ? startDate : LocalDate.now();

        // 1. Calculate Down Payment & Financed Amount
        BigDecimal downPaymentRatio = downPaymentPercentage.toDecimal();
        BigDecimal downPaymentVal = vehiclePrice.amount().multiply(downPaymentRatio).setScale(2, RoundingMode.HALF_UP);
        Money downPaymentAmount = new Money(downPaymentVal, currency);

        BigDecimal initialNetCapital = vehiclePrice.amount().subtract(downPaymentVal);

        // Vehicle Insurance Handling
        BigDecimal periodicVehicleInsVal = BigDecimal.ZERO;
        if (vehicleInsuranceType == VehicleInsuranceType.MENSUAL && vehicleInsuranceFee != null) {
            periodicVehicleInsVal = vehicleInsuranceFee.amount();
        } else if (vehicleInsuranceType == VehicleInsuranceType.FINANCIADO && vehicleInsuranceFee != null) {
            // Financed insurance: add total insurance amount to initial capital
            BigDecimal totalInsFee = vehicleInsuranceFee.amount().multiply(BigDecimal.valueOf(loanTermMonths));
            initialNetCapital = initialNetCapital.add(totalInsFee);
        }
        Money financedAmount = new Money(initialNetCapital, currency);

        // Balloon Payment Amount
        BigDecimal balloonRatio = balloonPaymentPercentage.toDecimal();
        BigDecimal balloonVal = vehiclePrice.amount().multiply(balloonRatio).setScale(2, RoundingMode.HALF_UP);
        Money balloonPaymentAmount = new Money(balloonVal, currency);

        // 2. Schedule Generation Setup
        List<PaymentPeriod> periods = new ArrayList<>();
        BigDecimal teaDecimal = annualEffectiveRate.toDecimal();
        BigDecimal desgravamenRate = monthlyCreditLifeInsuranceRate != null ? monthlyCreditLifeInsuranceRate.toDecimal() : BigDecimal.ZERO;

        int graceMonths = (gracePeriodType != GracePeriodType.NONE) ? Math.min(gracePeriodMonths, loanTermMonths - 1) : 0;
        int regularPeriodsCount = loanTermMonths - graceMonths;

        // Calculate initial regular monthly TEM for annuity factor
        double teaDouble = teaDecimal.doubleValue();
        double temMonthlyAvg = Math.pow(1.0 + teaDouble, 30.0 / 360.0) - 1.0;

        // First pass to simulate capital balance after grace period
        BigDecimal currentBalance = initialNetCapital;
        LocalDate currentDate = baseDate;

        List<Integer> daysInPeriodsList = new ArrayList<>();
        List<LocalDate> dueDatesList = new ArrayList<>();

        for (int p = 1; p <= loanTermMonths; p++) {
            LocalDate nextDate = baseDate.plusMonths(p);
            int days = (int) ChronoUnit.DAYS.between(currentDate, nextDate);
            daysInPeriodsList.add(days);
            dueDatesList.add(nextDate);
            currentDate = nextDate;
        }

        // Simulate Grace Periods
        for (int p = 0; p < graceMonths; p++) {
            int days = daysInPeriodsList.get(p);
            double temPeriod = Math.pow(1.0 + teaDouble, days / 360.0) - 1.0;
            BigDecimal interestAccrued = currentBalance.multiply(BigDecimal.valueOf(temPeriod)).setScale(2, RoundingMode.HALF_UP);

            if (gracePeriodType == GracePeriodType.TOTAL) {
                // Interest is capitalized
                currentBalance = currentBalance.add(interestAccrued);
            }
        }

        // Calculate French Regular Quota R for regular periods
        BigDecimal capitalAtGraceEnd = currentBalance;
        BigDecimal regularQuotaR = BigDecimal.ZERO;

        if (regularPeriodsCount > 0) {
            // Present Value of Balloon Payment at month graceMonths
            double pvBalloonFactor = Math.pow(1.0 + temMonthlyAvg, regularPeriodsCount);
            BigDecimal pvBalloon = balloonVal.divide(BigDecimal.valueOf(pvBalloonFactor), 6, RoundingMode.HALF_UP);
            BigDecimal amortizableCapital = capitalAtGraceEnd.subtract(pvBalloon);

            if (amortizableCapital.compareTo(BigDecimal.ZERO) < 0) {
                amortizableCapital = BigDecimal.ZERO;
            }

            if (temMonthlyAvg > 0) {
                double annuityFactor = (temMonthlyAvg * Math.pow(1.0 + temMonthlyAvg, regularPeriodsCount)) /
                        (Math.pow(1.0 + temMonthlyAvg, regularPeriodsCount) - 1.0);
                regularQuotaR = amortizableCapital.multiply(BigDecimal.valueOf(annuityFactor)).setScale(2, RoundingMode.HALF_UP);
            } else {
                regularQuotaR = amortizableCapital.divide(BigDecimal.valueOf(regularPeriodsCount), 2, RoundingMode.HALF_UP);
            }
        }

        // Build actual schedule
        currentBalance = initialNetCapital;
        BigDecimal totalInterestAcc = BigDecimal.ZERO;
        BigDecimal totalAmountAcc = BigDecimal.ZERO;

        for (int p = 1; p <= loanTermMonths; p++) {
            int days = daysInPeriodsList.get(p - 1);
            LocalDate dueDate = dueDatesList.get(p - 1);
            double temPeriod = Math.pow(1.0 + teaDouble, days / 360.0) - 1.0;

            BigDecimal initialBal = currentBalance;
            BigDecimal interestPayment = initialBal.multiply(BigDecimal.valueOf(temPeriod)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal creditLifeIns = initialBal.multiply(desgravamenRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal vehicleIns = periodicVehicleInsVal.setScale(2, RoundingMode.HALF_UP);

            BigDecimal amortization = BigDecimal.ZERO;
            GracePeriodType periodGrace = GracePeriodType.NONE;

            if (p <= graceMonths) {
                periodGrace = gracePeriodType;
                if (gracePeriodType == GracePeriodType.TOTAL) {
                    interestPayment = initialBal.multiply(BigDecimal.valueOf(temPeriod)).setScale(2, RoundingMode.HALF_UP);
                    amortization = BigDecimal.ZERO;
                    // In total grace, installment is 0 or ins only
                } else if (gracePeriodType == GracePeriodType.PARTIAL) {
                    amortization = BigDecimal.ZERO;
                }
            } else {
                if (p == loanTermMonths) {
                    // Final period: amortize all remaining balance (including balloon)
                    amortization = initialBal;
                } else {
                    amortization = regularQuotaR.subtract(interestPayment);
                    if (amortization.compareTo(BigDecimal.ZERO) < 0) {
                        amortization = BigDecimal.ZERO;
                    }
                    if (amortization.compareTo(initialBal) > 0) {
                        amortization = initialBal;
                    }
                }
            }

            BigDecimal totalInstallment;
            BigDecimal finalBal;

            if (periodGrace == GracePeriodType.TOTAL) {
                totalInstallment = creditLifeIns.add(vehicleIns);
                finalBal = initialBal.add(interestPayment);
            } else {
                totalInstallment = interestPayment.add(amortization).add(creditLifeIns).add(vehicleIns);
                finalBal = initialBal.subtract(amortization);
                if (finalBal.compareTo(BigDecimal.ZERO) < 0) {
                    finalBal = BigDecimal.ZERO;
                }
            }

            currentBalance = finalBal;
            totalInterestAcc = totalInterestAcc.add(interestPayment);
            totalAmountAcc = totalAmountAcc.add(totalInstallment);

            PaymentPeriod period = new PaymentPeriod(
                    p,
                    dueDate,
                    days,
                    new Money(initialBal, currency),
                    new Money(interestPayment, currency),
                    new Money(amortization, currency),
                    new Money(creditLifeIns, currency),
                    new Money(vehicleIns, currency),
                    new Money(totalInstallment, currency),
                    new Money(finalBal, currency),
                    periodGrace
            );
            periods.add(period);
        }

        // 3. Compute Financial Indicators (TIR, TCEA, VAN)
        BigDecimal initialFeesVal = (initialFees != null) ? initialFees.amount() : BigDecimal.ZERO;
        BigDecimal netDisbursed = initialNetCapital.subtract(initialFeesVal);

        double[] cashFlows = new double[loanTermMonths + 1];
        cashFlows[0] = -netDisbursed.doubleValue();
        for (int p = 1; p <= loanTermMonths; p++) {
            cashFlows[p] = periods.get(p - 1).getTotalInstallment().amount().doubleValue();
        }

        double monthlyIrr = calculateIRR(cashFlows);
        double annualTcea = Math.pow(1.0 + monthlyIrr, 12.0) - 1.0;
        if (annualTcea < 0 || Double.isNaN(annualTcea)) {
            annualTcea = teaDouble;
        }

        BigDecimal cokDecimal = discountRate != null ? discountRate.toDecimal() : BigDecimal.valueOf(0.10);
        double monthlyCok = Math.pow(1.0 + cokDecimal.doubleValue(), 1.0 / 12.0) - 1.0;

        double vanVal = cashFlows[0];
        for (int p = 1; p <= loanTermMonths; p++) {
            vanVal += cashFlows[p] / Math.pow(1.0 + monthlyCok, p);
        }

        Percent tcea = Percent.of(annualTcea * 100.0);
        Percent tir = Percent.of(monthlyIrr * 100.0);
        Money van = new Money(BigDecimal.valueOf(vanVal).setScale(2, RoundingMode.HALF_UP), currency);

        return new CalculationOutput(
                periods,
                financedAmount,
                downPaymentAmount,
                balloonPaymentAmount,
                tcea,
                tir,
                van,
                new Money(totalInterestAcc.setScale(2, RoundingMode.HALF_UP), currency),
                new Money(totalAmountAcc.setScale(2, RoundingMode.HALF_UP), currency)
        );
    }

    private void validateInputs(
            Money vehiclePrice,
            Percent downPaymentPercentage,
            Percent balloonPaymentPercentage,
            Percent annualEffectiveRate,
            int loanTermMonths,
            int gracePeriodMonths,
            GracePeriodType gracePeriodType
    ) {
        if (vehiclePrice == null || vehiclePrice.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("financing.error.vehiclePrice.invalid");
        }
        if (downPaymentPercentage == null) {
            throw new DomainValidationException("financing.error.downPaymentPercentage.required");
        }
        if (balloonPaymentPercentage == null) {
            throw new DomainValidationException("financing.error.balloonPaymentPercentage.required");
        }
        if (annualEffectiveRate == null) {
            throw new DomainValidationException("financing.error.annualEffectiveRate.required");
        }
        if (loanTermMonths <= 0) {
            throw new DomainValidationException("financing.error.loanTermMonths.invalid");
        }
        if (gracePeriodType != GracePeriodType.NONE && (gracePeriodMonths < 0 || gracePeriodMonths >= loanTermMonths)) {
            throw new DomainValidationException("financing.error.gracePeriodMonths.invalid");
        }
    }

    private double calculateIRR(double[] cashFlows) {
        double rate = 0.01; // initial guess 1% monthly
        for (int iter = 0; iter < 1000; iter++) {
            double npv = 0.0;
            double dNpv = 0.0;
            for (int t = 0; t < cashFlows.length; t++) {
                double factor = Math.pow(1.0 + rate, t);
                npv += cashFlows[t] / factor;
                if (t > 0) {
                    dNpv -= t * cashFlows[t] / (factor * (1.0 + rate));
                }
            }
            if (Math.abs(dNpv) < 1e-10) break;
            double newRate = rate - npv / dNpv;
            if (Math.abs(newRate - rate) < 1e-7) {
                return newRate;
            }
            rate = newRate;
        }
        return rate;
    }
}

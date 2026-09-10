package com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.entities.PaymentPeriod;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.VehicleInsuranceType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.services.FinancingPlanBuilder;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Simulation aggregate root representing a complete financial simulation plan.
 */
@Getter
public class Simulation extends AbstractDomainAggregateRoot<Simulation> {

    private final SimulationId id;
    private String title;
    private String userId;
    private String vehicleId;
    private String financialEntityId;
    private Money vehiclePrice;
    private Percent downPaymentPercentage;
    private Percent balloonPaymentPercentage;
    private Percent annualEffectiveRate;
    private Percent monthlyCreditLifeInsuranceRate;
    private Money vehicleInsuranceFee;
    private VehicleInsuranceType vehicleInsuranceType;
    private int loanTermMonths;
    private GracePeriodType gracePeriodType;
    private int gracePeriodMonths;
    private Money initialFees;
    private Percent discountRate;
    private LocalDate startDate;

    // Computed Output Fields
    private Money financedAmount;
    private Money downPaymentAmount;
    private Money balloonPaymentAmount;
    private Percent tcea;
    private Percent tir;
    private Money van;
    private Money totalInterest;
    private Money totalAmount;
    private final List<PaymentPeriod> paymentPeriods = new ArrayList<>();

    /**
     * Full constructor for reconstituting aggregate from persistence.
     */
    public Simulation(SimulationId id, String title, String userId, String vehicleId, String financialEntityId,
                      Money vehiclePrice, Percent downPaymentPercentage, Percent balloonPaymentPercentage,
                      Percent annualEffectiveRate, Percent monthlyCreditLifeInsuranceRate, Money vehicleInsuranceFee,
                      VehicleInsuranceType vehicleInsuranceType, int loanTermMonths, GracePeriodType gracePeriodType,
                      int gracePeriodMonths, Money initialFees, Percent discountRate, LocalDate startDate,
                      Money financedAmount, Money downPaymentAmount, Money balloonPaymentAmount, Percent tcea,
                      Percent tir, Money van, Money totalInterest, Money totalAmount, List<PaymentPeriod> paymentPeriods) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.financialEntityId = financialEntityId;
        this.vehiclePrice = vehiclePrice;
        this.downPaymentPercentage = downPaymentPercentage;
        this.balloonPaymentPercentage = balloonPaymentPercentage;
        this.annualEffectiveRate = annualEffectiveRate;
        this.monthlyCreditLifeInsuranceRate = monthlyCreditLifeInsuranceRate;
        this.vehicleInsuranceFee = vehicleInsuranceFee;
        this.vehicleInsuranceType = vehicleInsuranceType;
        this.loanTermMonths = loanTermMonths;
        this.gracePeriodType = gracePeriodType;
        this.gracePeriodMonths = gracePeriodMonths;
        this.initialFees = initialFees;
        this.discountRate = discountRate;
        this.startDate = startDate;
        this.financedAmount = financedAmount;
        this.downPaymentAmount = downPaymentAmount;
        this.balloonPaymentAmount = balloonPaymentAmount;
        this.tcea = tcea;
        this.tir = tir;
        this.van = van;
        this.totalInterest = totalInterest;
        this.totalAmount = totalAmount;
        if (paymentPeriods != null) {
            this.paymentPeriods.addAll(paymentPeriods);
        }
    }

    /**
     * Domain Constructor for creating a new Credit Simulation and calculating its financial plan.
     */
    public Simulation(String title, String userId, String vehicleId, String financialEntityId,
                      Money vehiclePrice, Percent downPaymentPercentage, Percent balloonPaymentPercentage,
                      Percent annualEffectiveRate, Percent monthlyCreditLifeInsuranceRate, Money vehicleInsuranceFee,
                      VehicleInsuranceType vehicleInsuranceType, int loanTermMonths, GracePeriodType gracePeriodType,
                      int gracePeriodMonths, Money initialFees, Percent discountRate, LocalDate startDate) {
        this.id = new SimulationId(UUID.randomUUID());
        setTitle(title);
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.financialEntityId = financialEntityId;
        this.vehiclePrice = vehiclePrice;
        this.downPaymentPercentage = downPaymentPercentage;
        this.balloonPaymentPercentage = balloonPaymentPercentage;
        this.annualEffectiveRate = annualEffectiveRate;
        this.monthlyCreditLifeInsuranceRate = monthlyCreditLifeInsuranceRate;
        this.vehicleInsuranceFee = vehicleInsuranceFee;
        this.vehicleInsuranceType = vehicleInsuranceType != null ? vehicleInsuranceType : VehicleInsuranceType.ENDOSADO;
        this.loanTermMonths = loanTermMonths;
        this.gracePeriodType = gracePeriodType != null ? gracePeriodType : GracePeriodType.NONE;
        this.gracePeriodMonths = gracePeriodMonths;
        this.initialFees = initialFees != null ? initialFees : Money.zero(vehiclePrice.currency());
        this.discountRate = discountRate != null ? discountRate : Percent.of(10.0);
        this.startDate = startDate != null ? startDate : LocalDate.now();

        // Calculate schedule and metrics using FinancingPlanBuilder
        calculatePlan();
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainValidationException("financing.error.simulation.title.required");
        }
        this.title = title.trim();
    }

    /**
     * Invokes FinancingPlanBuilder to compute or recalculate schedule and financial metrics.
     */
    public void calculatePlan() {
        FinancingPlanBuilder builder = new FinancingPlanBuilder();
        FinancingPlanBuilder.CalculationOutput output = builder.buildPlan(
                this.vehiclePrice,
                this.downPaymentPercentage,
                this.balloonPaymentPercentage,
                this.annualEffectiveRate,
                this.monthlyCreditLifeInsuranceRate,
                this.vehicleInsuranceFee,
                this.vehicleInsuranceType,
                this.loanTermMonths,
                this.gracePeriodType,
                this.gracePeriodMonths,
                this.initialFees,
                this.discountRate,
                this.startDate
        );

        this.financedAmount = output.financedAmount();
        this.downPaymentAmount = output.downPaymentAmount();
        this.balloonPaymentAmount = output.balloonPaymentAmount();
        this.tcea = output.tcea();
        this.tir = output.tir();
        this.van = output.van();
        this.totalInterest = output.totalInterest();
        this.totalAmount = output.totalAmount();

        this.paymentPeriods.clear();
        this.paymentPeriods.addAll(output.periods());
    }

    public List<PaymentPeriod> getPaymentPeriods() {
        return Collections.unmodifiableList(paymentPeriods);
    }
}

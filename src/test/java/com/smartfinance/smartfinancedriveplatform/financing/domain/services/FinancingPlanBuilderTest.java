package com.smartfinance.smartfinancedriveplatform.financing.domain.services;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.entities.PaymentPeriod;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.VehicleInsuranceType;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FinancingPlanBuilder Domain Service Tests")
class FinancingPlanBuilderTest {

    private FinancingPlanBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new FinancingPlanBuilder();
    }

    @Test
    @DisplayName("Should generate valid French payment schedule for standard credit without grace")
    void shouldGenerateStandardPaymentSchedule() {
        Money vehiclePrice = Money.of(20000.0, "USD");
        Percent downPayment = Percent.of(20.0); // 4,000 USD -> 16,000 USD financed
        Percent balloon = Percent.of(0.0);
        Percent tea = Percent.of(12.0);
        Percent desgravamen = Percent.of(0.05);
        Money vehicleInsFee = Money.of(50.0, "USD");
        VehicleInsuranceType vehicleInsType = VehicleInsuranceType.MENSUAL;
        int loanTermMonths = 24;
        GracePeriodType graceType = GracePeriodType.NONE;
        int graceMonths = 0;
        Money initialFees = Money.of(100.0, "USD");
        Percent discountRate = Percent.of(10.0);
        LocalDate startDate = LocalDate.of(2026, 1, 1);

        FinancingPlanBuilder.CalculationOutput output = builder.buildPlan(
                vehiclePrice, downPayment, balloon, tea, desgravamen, vehicleInsFee,
                vehicleInsType, loanTermMonths, graceType, graceMonths, initialFees,
                discountRate, startDate
        );

        assertNotNull(output);
        assertEquals(24, output.periods().size());
        assertEquals(new BigDecimal("16000.00"), output.financedAmount().amount());
        assertEquals(new BigDecimal("4000.00"), output.downPaymentAmount().amount());
        assertEquals(new BigDecimal("0.00"), output.balloonPaymentAmount().amount());

        PaymentPeriod lastPeriod = output.periods().get(23);
        assertEquals(24, lastPeriod.getPeriodNumber());
        assertEquals(new BigDecimal("0.00"), lastPeriod.getFinalBalance().amount());

        assertTrue(output.tcea().value().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(output.totalAmount().amount().compareTo(output.financedAmount().amount()) > 0);
    }

    @Test
    @DisplayName("Should handle total grace period with capitalized interest")
    void shouldHandleTotalGracePeriod() {
        Money vehiclePrice = Money.of(15000.0, "USD");
        Percent downPayment = Percent.of(10.0); // 1,500 USD -> 13,500 USD financed
        Percent balloon = Percent.of(0.0);
        Percent tea = Percent.of(14.0);
        Percent desgravamen = Percent.of(0.05);
        Money vehicleInsFee = Money.of(30.0, "USD");
        VehicleInsuranceType vehicleInsType = VehicleInsuranceType.MENSUAL;
        int loanTermMonths = 12;
        GracePeriodType graceType = GracePeriodType.TOTAL;
        int graceMonths = 2;
        Money initialFees = Money.zero("USD");
        Percent discountRate = Percent.of(10.0);
        LocalDate startDate = LocalDate.of(2026, 1, 1);

        FinancingPlanBuilder.CalculationOutput output = builder.buildPlan(
                vehiclePrice, downPayment, balloon, tea, desgravamen, vehicleInsFee,
                vehicleInsType, loanTermMonths, graceType, graceMonths, initialFees,
                discountRate, startDate
        );

        assertEquals(12, output.periods().size());

        PaymentPeriod p1 = output.periods().get(0);
        assertEquals(GracePeriodType.TOTAL, p1.getGraceType());
        assertEquals(new BigDecimal("0.00"), p1.getPrincipalAmortization().amount());
        assertTrue(p1.getFinalBalance().amount().compareTo(p1.getInitialBalance().amount()) > 0);

        PaymentPeriod p2 = output.periods().get(1);
        assertEquals(GracePeriodType.TOTAL, p2.getGraceType());

        PaymentPeriod lastPeriod = output.periods().get(11);
        assertEquals(new BigDecimal("0.00"), lastPeriod.getFinalBalance().amount());
    }

    @Test
    @DisplayName("Should handle Smart Buy (Compra Inteligente) with balloon payment at end")
    void shouldHandleSmartBuyWithBalloon() {
        Money vehiclePrice = Money.of(30000.0, "USD");
        Percent downPayment = Percent.of(20.0); // 6,000 USD down -> 24,000 USD financed
        Percent balloon = Percent.of(30.0); // 9,000 USD balloon payment
        Percent tea = Percent.of(10.0);
        Percent desgravamen = Percent.of(0.04);
        Money vehicleInsFee = Money.zero("USD");
        VehicleInsuranceType vehicleInsType = VehicleInsuranceType.ENDOSADO;
        int loanTermMonths = 36;
        GracePeriodType graceType = GracePeriodType.NONE;
        int graceMonths = 0;
        Money initialFees = Money.of(150.0, "USD");
        Percent discountRate = Percent.of(10.0);
        LocalDate startDate = LocalDate.of(2026, 1, 1);

        FinancingPlanBuilder.CalculationOutput output = builder.buildPlan(
                vehiclePrice, downPayment, balloon, tea, desgravamen, vehicleInsFee,
                vehicleInsType, loanTermMonths, graceType, graceMonths, initialFees,
                discountRate, startDate
        );

        assertEquals(36, output.periods().size());
        assertEquals(new BigDecimal("9000.00"), output.balloonPaymentAmount().amount());

        PaymentPeriod p35 = output.periods().get(34);
        assertTrue(p35.getFinalBalance().amount().compareTo(new BigDecimal("8000.00")) > 0);

        PaymentPeriod p36 = output.periods().get(35);
        assertEquals(new BigDecimal("0.00"), p36.getFinalBalance().amount());
    }
}

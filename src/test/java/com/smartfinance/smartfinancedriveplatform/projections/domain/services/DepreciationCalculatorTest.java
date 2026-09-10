package com.smartfinance.smartfinancedriveplatform.projections.domain.services;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.MotorizationType;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.RecommendedAction;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DepreciationCalculator Domain Service Tests")
class DepreciationCalculatorTest {

    private DepreciationCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DepreciationCalculator();
    }

    @Test
    @DisplayName("Should calculate combustion vehicle depreciation curve correctly")
    void shouldCalculateCombustionDepreciation() {
        Money initialPrice = Money.of(20000.0, "USD");
        MotorizationType type = MotorizationType.COMBUSTION;
        Money balloon = Money.of(10000.0, "USD");

        DepreciationCalculator.CalculationOutput output = calculator.calculateProjection(initialPrice, type, balloon);

        assertNotNull(output);
        // Year 1 = 20,000 * 0.82 = 16,400
        // Year 2 = 16,400 * 0.90 = 14,760
        // Year 3 = 14,760 * 0.90 = 13,284
        assertEquals(new BigDecimal("14760.00"), output.projectedValue2Years().amount());
        assertEquals(new BigDecimal("13284.00"), output.projectedValue3Years().amount());
        assertEquals(RecommendedAction.TRADE_IN, output.recommendedAction()); // 13,284 > 10,000 * 1.15 = 11,500
    }

    @Test
    @DisplayName("Should calculate eco vehicle depreciation curve with higher value retention")
    void shouldCalculateEcoDepreciation() {
        Money initialPrice = Money.of(30000.0, "USD");
        MotorizationType type = MotorizationType.ECOLOGICO;
        Money balloon = Money.of(22000.0, "USD");

        DepreciationCalculator.CalculationOutput output = calculator.calculateProjection(initialPrice, type, balloon);

        assertNotNull(output);
        // Year 1 = 30,000 * 0.88 = 26,400
        // Year 2 = 26,400 * 0.93 = 24,552
        // Year 3 = 24,552 * 0.93 = 22,833.36
        assertEquals(new BigDecimal("24552.00"), output.projectedValue2Years().amount());
        assertEquals(RecommendedAction.KEEP_AND_PAY, output.recommendedAction()); // 22,833.36 is >= 22,000 but <= 25,300
    }

    @Test
    @DisplayName("Should recommend RETURN_VEHICLE when market value drops below balloon payment")
    void shouldRecommendReturnVehicleWhenEquityIsNegative() {
        Money initialPrice = Money.of(20000.0, "USD");
        MotorizationType type = MotorizationType.COMBUSTION;
        Money highBalloon = Money.of(15000.0, "USD"); // Year 3 value is 13,284 < 15,000

        DepreciationCalculator.CalculationOutput output = calculator.calculateProjection(initialPrice, type, highBalloon);

        assertEquals(RecommendedAction.RETURN_VEHICLE, output.recommendedAction());
    }
}

package com.smartfinance.smartfinancedriveplatform.consultations.domain.services;

import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AiAdvisorEngine Domain Service Unit Tests")
class AiAdvisorEngineTest {

    private final AiAdvisorEngine engine = new AiAdvisorEngine();

    @Test
    @DisplayName("Should generate 30 percent max monthly fee and SUV recommendation for high income")
    void shouldGenerateAdviceForHighIncome() {
        var advice = engine.generateAdvice(
                "¿Qué auto me conviene para viajar con familia?",
                new Money(new BigDecimal("10000"), "PEN"),
                null
        );

        assertThat(advice.estimatedMaxMonthlyFee()).isEqualTo(3000.0);
        assertThat(advice.recommendedVehicleCategory()).contains("SUV Premium");
        assertThat(advice.recommendationText()).contains("30%");
    }
}

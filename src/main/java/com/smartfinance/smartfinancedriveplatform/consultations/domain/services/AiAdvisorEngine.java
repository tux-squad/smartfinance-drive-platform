package com.smartfinance.smartfinancedriveplatform.consultations.domain.services;

import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain Service for calculating AI financial recommendations and vehicle affordability.
 */
@Service
public class AiAdvisorEngine {

    public record RecommendationResult(
        String recommendationText,
        String recommendedVehicleCategory,
        Double estimatedMaxMonthlyFee
    ) {}

    public RecommendationResult generateAdvice(String prompt, Money monthlyIncome, Money maxBudget) {
        BigDecimal income = (monthlyIncome != null && monthlyIncome.amount() != null) 
                ? monthlyIncome.amount() 
                : BigDecimal.valueOf(3500);

        // Financial 30% rule for max vehicle monthly payment
        BigDecimal maxMonthlyFee = income.multiply(BigDecimal.valueOf(0.30)).setScale(2, RoundingMode.HALF_UP);
        double maxMonthlyFeeDouble = maxMonthlyFee.doubleValue();

        String category;
        if (maxMonthlyFeeDouble > 2000) {
            category = "SUV Premium / Pick-Up 4x4";
        } else if (maxMonthlyFeeDouble > 1000) {
            category = "SUV Crossover / Sedan Ejecutivo";
        } else {
            category = "Hatchback / Sedan Urbano";
        }

        StringBuilder advice = new StringBuilder();
        advice.append("Basado en un ingreso mensual de ").append(income).append(" PEN/USD, ");
        advice.append("la regla financiera recomendada limita tu cuota mensual máxima a ").append(maxMonthlyFee).append(" (30% del ingreso). ");
        advice.append("Te recomendamos buscar vehículos en la categoría '").append(category).append("'. ");

        if (prompt != null && !prompt.isBlank()) {
            advice.append("Respecto a tu consulta: \"").append(prompt.trim()).append("\", ");
            advice.append("te sugerimos comparar las TEA promocionales de los bancos asociados (BCP, BBVA, Interbank) para optimizar tus gastos de seguro y mantenimiento.");
        } else {
            advice.append("Recuerda simular tu plan de pagos con tasa efectiva anual (TEA) preferencial antes de solicitar tu crédito.");
        }

        return new RecommendationResult(advice.toString(), category, maxMonthlyFeeDouble);
    }
}

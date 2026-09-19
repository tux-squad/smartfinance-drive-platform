package com.smartfinance.smartfinancedriveplatform.consultations.domain.services;

import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Domain Service for calculating AI financial recommendations and vehicle affordability.
 * Integrates Google Gemini API with graceful fallback to rule-based financial calculation.
 */
@Service
public class AiAdvisorEngine {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${google.ai.api-key:}")
    private String googleAiApiKey;

    private final RestTemplate restTemplate;

    public AiAdvisorEngine() {
        this.restTemplate = new RestTemplate();
    }

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

        String activeKey = (geminiApiKey != null && !geminiApiKey.isBlank()) ? geminiApiKey : googleAiApiKey;
        if (activeKey != null && !activeKey.isBlank()) {
            try {
                String llmAdvice = callGeminiApi(activeKey, prompt, income, maxMonthlyFee, category);
                if (llmAdvice != null && !llmAdvice.isBlank()) {
                    return new RecommendationResult(llmAdvice, category, maxMonthlyFeeDouble);
                }
            } catch (Exception ignored) {
                // Fallback to rule engine on API error or timeout
            }
        }

        // Fallback rule engine
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

    private String callGeminiApi(String apiKey, String userPrompt, BigDecimal income, BigDecimal maxMonthlyFee, String category) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String promptContent = String.format(
            "Actúa como un asesor financiero experto de SmartFinance Drive Platform. El usuario gana %s al mes. Su cuota mensual máxima recomendada es %s en la categoría %s. Consulta del usuario: '%s'. Proporciona un consejo financiero breve y útil (máximo 3 oraciones) en español.",
            income, maxMonthlyFee, category, (userPrompt != null ? userPrompt : "Recomendación general")
        );

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", promptContent)
                ))
            )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        Map<?, ?> response = restTemplate.postForObject(url, entity, Map.class);
        if (response != null && response.containsKey("candidates")) {
            List<?> candidates = (List<?>) response.get("candidates");
            if (!candidates.isEmpty() && candidates.get(0) instanceof Map<?, ?> candidateMap) {
                if (candidateMap.get("content") instanceof Map<?, ?> contentMap) {
                    if (contentMap.get("parts") instanceof List<?> parts && !parts.isEmpty()) {
                        if (parts.get(0) instanceof Map<?, ?> partMap && partMap.get("text") instanceof String text) {
                            return text.trim();
                        }
                    }
                }
            }
        }
        return null;
    }
}


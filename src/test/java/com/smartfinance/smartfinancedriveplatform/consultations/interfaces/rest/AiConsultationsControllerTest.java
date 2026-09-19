package com.smartfinance.smartfinancedriveplatform.consultations.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.consultations.application.commandservices.AiConsultationCommandService;
import com.smartfinance.smartfinancedriveplatform.consultations.application.queryservices.AiConsultationQueryService;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.aggregates.AiConsultation;
import com.smartfinance.smartfinancedriveplatform.consultations.domain.model.queries.GetAiConsultationHistoryForUserQuery;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AiConsultationsController Unit Tests")
class AiConsultationsControllerTest {

    private AiConsultationCommandService commandService;
    private AiConsultationQueryService queryService;
    private VehicleQueryService vehicleQueryService;
    private AiConsultationsController controller;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        commandService = mock(AiConsultationCommandService.class);
        queryService = mock(AiConsultationQueryService.class);
        vehicleQueryService = mock(VehicleQueryService.class);
        controller = new AiConsultationsController(commandService, queryService, vehicleQueryService);

        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getRequiredCurrentUserId).thenReturn("user-123");
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("Should return consultation history")
    void shouldReturnConsultationHistory() {
        AiConsultation consultation = new AiConsultation(
                "user-123",
                "Recomendacion vehiculo",
                null,
                null,
                "Basado en tu ingreso...",
                "session-1",
                0.95
        );
        when(queryService.handle(any(GetAiConsultationHistoryForUserQuery.class))).thenReturn(List.of(consultation));

        var response = controller.getConsultationHistory();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("Should return vehicle recommendations")
    void shouldReturnVehicleRecommendations() {
        when(vehicleQueryService.handle(any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        var response = controller.getAiVehicleRecommendations();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}

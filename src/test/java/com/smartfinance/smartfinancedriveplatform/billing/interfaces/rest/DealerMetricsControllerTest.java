package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.DealerMetricsResource;
import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.ProspectRepository;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DealerMetricsController REST Unit Tests")
class DealerMetricsControllerTest {

    @Mock
    private ProspectRepository prospectRepository;

    @Mock
    private VehicleQueryService vehicleQueryService;

    @InjectMocks
    private DealerMetricsController controller;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "dealer-123", "password", Collections.emptyList()
        );
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails("dealer-123", "dealer-123"));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return metrics resource for authenticated dealer")
    void shouldReturnDealerMetrics() {
        Prospect p1 = new Prospect("dealer-123", "buyer-1", "Ana Gomez", "ana@test.com", "999000111", UUID.randomUUID(), "agent-1");
        p1.setStatus("CLOSED_WON");

        when(prospectRepository.findAllByDealerUserId("dealer-123")).thenReturn(List.of(p1));
        when(vehicleQueryService.handle(any(GetVehiclesByUserIdQuery.class))).thenReturn(Collections.emptyList());

        ResponseEntity<DealerMetricsResource> response = controller.getDealerMetrics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().totalLeadsGenerated());
        assertEquals(100.0, response.getBody().conversionRate());
        assertEquals("LAST_30_DAYS", response.getBody().period());
    }
}

package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.analytics.application.queryservices.AnalyticsQueryService;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetAdminDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetFinancialInstitutionDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.*;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.AdminDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.DealerDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.FinancialInstitutionDashboardResource;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsController REST Unit Tests")
class AnalyticsControllerTest {

    @Mock
    private AnalyticsQueryService analyticsQueryService;

    @InjectMocks
    private AnalyticsController analyticsController;

    private final String dealerUserId = "dealer-user-123";
    private final UUID bankEntityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                dealerUserId, "password", Collections.emptyList()
        );
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails(dealerUserId, dealerUserId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return dealer dashboard metrics with authenticated user fallback")
    void shouldReturnDealerDashboardMetrics() {
        DealerInventoryMetrics inventory = new DealerInventoryMetrics(10, 7, 2, 1, BigDecimal.valueOf(250000), BigDecimal.valueOf(35000));
        DealerCrmMetrics crm = new DealerCrmMetrics(20, 5, 5, 4, 3, 2, 1, 10.0);
        DealerTestDriveMetrics testDrives = new DealerTestDriveMetrics(8, 2, 3, 2, 1);
        DealerFinancingMetrics financing = new DealerFinancingMetrics(6, 3, 2, 1);

        DealerDashboardMetrics metrics = new DealerDashboardMetrics(
                dealerUserId, inventory, crm, testDrives, financing, 450, "4.8x", "LAST_30_DAYS"
        );

        when(analyticsQueryService.handle(any(GetDealerDashboardMetricsQuery.class))).thenReturn(metrics);

        ResponseEntity<DealerDashboardResource> response = analyticsController.getDealerMetrics(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(dealerUserId, response.getBody().dealerUserId());
        assertEquals(10, response.getBody().inventory().totalVehicles());
        assertEquals(20, response.getBody().crm().totalLeads());
        assertEquals(8, response.getBody().testDrives().totalTestDrives());
        assertEquals(6, response.getBody().financing().totalApplicationsReceived());
    }

    @Test
    @DisplayName("Should return financial institution dashboard metrics when found")
    void shouldReturnFinancialInstitutionDashboardMetrics() {
        FinancialInstitutionDashboardMetrics metrics = new FinancialInstitutionDashboardMetrics(
                bankEntityId,
                "Banco Continental",
                15,
                4,
                8,
                2,
                1,
                53.3,
                BigDecimal.valueOf(450000),
                BigDecimal.valueOf(180000),
                BigDecimal.valueOf(13.9),
                3
        );

        when(analyticsQueryService.handle(new GetFinancialInstitutionDashboardMetricsQuery(bankEntityId)))
                .thenReturn(Optional.of(metrics));

        ResponseEntity<FinancialInstitutionDashboardResource> response =
                analyticsController.getFinancialInstitutionMetrics(bankEntityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(bankEntityId, response.getBody().financialEntityId());
        assertEquals("Banco Continental", response.getBody().financialEntityName());
        assertEquals(15, response.getBody().totalApplicationsReceived());
        assertEquals(53.3, response.getBody().approvalRate());
        assertEquals(BigDecimal.valueOf(13.9), response.getBody().averageTea());
    }

    @Test
    @DisplayName("Should return 404 Not Found when financial institution does not exist")
    void shouldReturn404WhenFinancialInstitutionNotFound() {
        when(analyticsQueryService.handle(new GetFinancialInstitutionDashboardMetricsQuery(bankEntityId)))
                .thenReturn(Optional.empty());

        ResponseEntity<FinancialInstitutionDashboardResource> response =
                analyticsController.getFinancialInstitutionMetrics(bankEntityId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Should return admin platform-wide dashboard metrics")
    void shouldReturnAdminDashboardMetrics() {
        AdminDashboardMetrics metrics = new AdminDashboardMetrics(
                12,
                10,
                5,
                150,
                320,
                45,
                80,
                8,
                BigDecimal.valueOf(1600.00)
        );

        when(analyticsQueryService.handle(any(GetAdminDashboardMetricsQuery.class))).thenReturn(metrics);

        ResponseEntity<AdminDashboardResource> response = analyticsController.getAdminMetrics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(12, response.getBody().totalDealerships());
        assertEquals(10, response.getBody().activeDealerships());
        assertEquals(5, response.getBody().totalFinancialEntities());
        assertEquals(150, response.getBody().totalRegisteredUsers());
        assertEquals(8, response.getBody().totalActiveSubscriptions());
        assertEquals(BigDecimal.valueOf(1600.00), response.getBody().estimatedMonthlyRecurringRevenueUsd());
    }
}

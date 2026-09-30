package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.analytics.application.queryservices.AnalyticsQueryService;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetAdminDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetFinancialInstitutionDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.*;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.OwnershipChecker;
import com.smartfinance.smartfinancedriveplatform.shared.interfaces.rest.setup.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AnalyticsController.class, GlobalExceptionHandler.class})
@Import(AnalyticsWebMvcSecurityTest.SecurityTestConfig.class)
@DisplayName("Analytics WebMvc Security & SpEL Authorization Tests")
class AnalyticsWebMvcSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsQueryService analyticsQueryService;

    @MockitoBean
    private FinancialEntityQueryService financialEntityQueryService;

    @MockitoBean(name = "ownershipChecker")
    private OwnershipChecker ownershipChecker;

    @MockitoBean
    private com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline.RateLimitingFilter rateLimitingFilter;

    @MockitoBean
    private org.springframework.cache.CacheManager cacheManager;

    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(rateLimitingFilter).doFilter(any(), any(), any());
    }

    private DealerDashboardMetrics sampleDealerMetrics(String dealerId) {
        return new DealerDashboardMetrics(
                dealerId,
                new DealerInventoryMetrics(5, 4, 1, 0, BigDecimal.valueOf(100000), BigDecimal.ZERO),
                new DealerCrmMetrics(10, 3, 2, 2, 1, 2, 0, 20.0),
                new DealerTestDriveMetrics(3, 1, 1, 1, 0),
                new DealerFinancingMetrics(4, 2, 1, 1),
                "ALL_TIME"
        );
    }

    private FinancialInstitutionDashboardMetrics sampleBankMetrics(UUID bankId) {
        return new FinancialInstitutionDashboardMetrics(
                bankId,
                "Banco BCP",
                20,
                5,
                10,
                3,
                2,
                60.0,
                BigDecimal.valueOf(500000),
                BigDecimal.valueOf(250000),
                BigDecimal.valueOf(14.5),
                3,
                18
        );
    }

    // ==========================================
    // Dealer Security & IDOR Tests
    // ==========================================

    @Test
    @WithMockUser(username = "dealer-1", roles = "DEALER")
    @DisplayName("DEALER querying own metrics without dealerUserId parameter should succeed (200 OK)")
    void dealerCanQueryOwnMetricsWithoutParam() throws Exception {
        when(analyticsQueryService.handle(any(GetDealerDashboardMetricsQuery.class)))
                .thenReturn(sampleDealerMetrics("dealer-1"));

        mockMvc.perform(get("/api/v1/analytics/dealer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealerUserId").value("dealer-1"));
    }

    @Test
    @WithMockUser(username = "dealer-1", roles = "DEALER")
    @DisplayName("DEALER querying own metrics with empty dealerUserId parameter should fallback and succeed (200 OK)")
    void dealerCanQueryOwnMetricsWithEmptyParam() throws Exception {
        when(analyticsQueryService.handle(any(GetDealerDashboardMetricsQuery.class)))
                .thenReturn(sampleDealerMetrics("dealer-1"));

        mockMvc.perform(get("/api/v1/analytics/dealer").param("dealerUserId", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealerUserId").value("dealer-1"));
    }

    @Test
    @WithMockUser(username = "dealer-1", roles = "DEALER")
    @DisplayName("DEALER querying own metrics explicitly providing own ID should succeed (200 OK)")
    void dealerCanQueryOwnMetricsWithOwnId() throws Exception {
        when(ownershipChecker.isUserSelfStr(eq("dealer-1"), any())).thenReturn(true);
        when(analyticsQueryService.handle(any(GetDealerDashboardMetricsQuery.class)))
                .thenReturn(sampleDealerMetrics("dealer-1"));

        mockMvc.perform(get("/api/v1/analytics/dealer").param("dealerUserId", "dealer-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealerUserId").value("dealer-1"));
    }

    @Test
    @WithMockUser(username = "dealer-1", roles = "DEALER")
    @DisplayName("DEALER attempting IDOR by querying another dealer's ID should be blocked by SpEL with 403 Forbidden")
    void dealerCannotQueryOtherDealerMetrics_IdorBlocked() throws Exception {
        when(ownershipChecker.isUserSelfStr(eq("dealer-2"), any())).thenReturn(false);

        mockMvc.perform(get("/api/v1/analytics/dealer").param("dealerUserId", "dealer-2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin-1", roles = "ADMIN")
    @DisplayName("ADMIN can query any dealer's metrics (200 OK)")
    void adminCanQueryAnyDealerMetrics() throws Exception {
        when(analyticsQueryService.handle(any(GetDealerDashboardMetricsQuery.class)))
                .thenReturn(sampleDealerMetrics("dealer-2"));

        mockMvc.perform(get("/api/v1/analytics/dealer").param("dealerUserId", "dealer-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealerUserId").value("dealer-2"));
    }

    @Test
    @WithMockUser(username = "dealer-1", roles = "DEALER")
    @DisplayName("Invalid period parameter should return 400 Bad Request")
    void invalidPeriodReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dealer").param("period", "INVALID_PERIOD"))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // Financial Institution Security & IDOR Tests
    // ==========================================

    @Test
    @WithMockUser(username = "bank-user-1", roles = "FINANCIAL_INSTITUTION")
    @DisplayName("FINANCIAL_INSTITUTION querying with no param resolves own bank (200 OK)")
    void financialInstitutionCanQueryOwnMetricsWithoutParam() throws Exception {
        UUID bankId = UUID.randomUUID();
        FinancialEntity entity = new FinancialEntity(
                new FinancialEntityId(bankId),
                "bank-user-1",
                "Banco BCP",
                null,
                null,
                Collections.emptyList()
        );

        when(financialEntityQueryService.handle(any(GetFinancialEntityByUserIdQuery.class)))
                .thenReturn(Optional.of(entity));
        when(analyticsQueryService.handle(any(GetFinancialInstitutionDashboardMetricsQuery.class)))
                .thenReturn(Optional.of(sampleBankMetrics(bankId)));

        mockMvc.perform(get("/api/v1/analytics/financial-institution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financialEntityName").value("Banco BCP"));
    }

    @Test
    @WithMockUser(username = "bank-user-1", roles = "FINANCIAL_INSTITUTION")
    @DisplayName("FINANCIAL_INSTITUTION attempting IDOR to read another bank should be blocked with 403 Forbidden")
    void financialInstitutionCannotQueryOtherBank_IdorBlocked() throws Exception {
        UUID otherBankId = UUID.randomUUID();
        when(ownershipChecker.isFinancialEntityOwner(eq(otherBankId), any())).thenReturn(false);

        mockMvc.perform(get("/api/v1/analytics/financial-institution")
                        .param("financialEntityId", otherBankId.toString()))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // Role Authorization Tests
    // ==========================================

    @Test
    @WithMockUser(username = "dealer-1", roles = "DEALER")
    @DisplayName("DEALER attempting to access ADMIN dashboard should receive 403 Forbidden")
    void nonAdminCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin-1", roles = "ADMIN")
    @DisplayName("ADMIN accessing ADMIN dashboard should receive 200 OK")
    void adminCanAccessAdminDashboard() throws Exception {
        AdminDashboardMetrics adminMetrics = new AdminDashboardMetrics(
                10, 8, 4, 120, 45, 60, 200, 15, BigDecimal.valueOf(3500.00)
        );
        when(analyticsQueryService.handle(any(GetAdminDashboardMetricsQuery.class)))
                .thenReturn(adminMetrics);

        mockMvc.perform(get("/api/v1/analytics/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDealerships").value(10))
                .andExpect(jsonPath("$.estimatedMonthlyRecurringRevenueUsd").value(3500.00));
    }
}

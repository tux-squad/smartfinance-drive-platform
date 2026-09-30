package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.FinancialEntityCommandService;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.OwnershipChecker;
import com.smartfinance.smartfinancedriveplatform.shared.interfaces.rest.setup.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {FinancialEntitiesController.class, GlobalExceptionHandler.class})
@Import(FinancialEntitiesWebMvcSecurityTest.SecurityTestConfig.class)
@DisplayName("FinancialEntities WebMvc Security & Authorization Tests")
class FinancialEntitiesWebMvcSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinancialEntityCommandService financialEntityCommandService;

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

    @BeforeEach
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

    @Test
    @DisplayName("PUT /{id} should return 403 Forbidden when FI is not owner")
    @WithMockUser(username = "attacker-fi", roles = {"FINANCIAL_INSTITUTION"})
    void putEntityForbiddenWhenNotOwner() throws Exception {
        UUID entityId = UUID.randomUUID();
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(false);

        mockMvc.perform(put("/api/v1/financial-entities/" + entityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Malicious Hijack\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /{id}/rate-benchmarks should return 403 Forbidden when FI is not owner")
    @WithMockUser(username = "attacker-fi", roles = {"FINANCIAL_INSTITUTION"})
    void postRateBenchmarkForbiddenWhenNotOwner() throws Exception {
        UUID entityId = UUID.randomUUID();
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(false);

        String payload = """
                {
                    "rateType": "TEA",
                    "annualRate": 14.5,
                    "currency": "PEN",
                    "sourceLabel": "SBS",
                    "sourceUrl": "https://sbs.gob.pe",
                    "effectiveFrom": "2026-01-01"
                }
                """;

        mockMvc.perform(post("/api/v1/financial-entities/" + entityId + "/rate-benchmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST / should return 403 Forbidden when FI tries to impersonate another userId")
    @WithMockUser(username = "fi-user", roles = {"FINANCIAL_INSTITUTION"})
    void postEntityForbiddenWhenImpersonatingUserId() throws Exception {
        String payload = """
                {
                    "userId": "other-user-999",
                    "name": "Spoofed Bank",
                    "ruc": "20100047218"
                }
                """;

        mockMvc.perform(post("/api/v1/financial-entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }
}

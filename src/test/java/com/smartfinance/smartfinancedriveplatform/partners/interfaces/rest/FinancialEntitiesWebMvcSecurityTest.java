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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    private com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.storage.FinancialEntityImageStorageService imageStorageService;

    @MockitoBean
    private com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline.RateLimitingFilter rateLimitingFilter;

    @MockitoBean
    private org.springframework.cache.CacheManager cacheManager;

    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.security.web.SecurityFilterChain testSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            http
                    .csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                    .sessionManagement(s -> s.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                    .exceptionHandling(e -> e.authenticationEntryPoint(new org.springframework.security.web.authentication.HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
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

    @Test
    @DisplayName("POST /me/logo should return 403 Forbidden for USER role")
    @WithMockUser(username = "regular-user", roles = {"USER"})
    void postMyLogoForbiddenForUserRole() throws Exception {
        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "logo.png", "image/png", "sample".getBytes());

        mockMvc.perform(multipart("/api/v1/financial-entities/me/logo").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /{id}/logo should return 403 Forbidden when FI is not owner")
    @WithMockUser(username = "attacker-fi", roles = {"FINANCIAL_INSTITUTION"})
    void postLogoByIdForbiddenWhenNotOwner() throws Exception {
        UUID entityId = UUID.randomUUID();
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(false);

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "logo.png", "image/png", "sample".getBytes());

        mockMvc.perform(multipart("/api/v1/financial-entities/" + entityId + "/logo").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /me/logo should succeed for authenticated FINANCIAL_INSTITUTION")
    @WithMockUser(username = "bank-owner", roles = {"FINANCIAL_INSTITUTION"})
    void postMyLogoAllowedForFinancialInstitution() throws Exception {
        UUID entityId = UUID.randomUUID();
        var entity = new com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity(
                new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(entityId),
                "bank-owner", "20100047218", "Banco BCP", null, null, java.util.Collections.emptyList()
        );

        when(financialEntityQueryService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByUserIdQuery.class)))
                .thenReturn(java.util.Optional.of(entity));
        when(imageStorageService.uploadFinancialEntityImage(any(), eq("logos")))
                .thenReturn("https://cdn.example.com/logo.png");
        when(financialEntityCommandService.updateLogo(any(), any()))
                .thenReturn(java.util.Optional.of(entity));

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "logo.png", "image/png", "sample".getBytes());

        mockMvc.perform(multipart("/api/v1/financial-entities/me/logo").file(file))
                .andExpect(status().isOk());
    }
}

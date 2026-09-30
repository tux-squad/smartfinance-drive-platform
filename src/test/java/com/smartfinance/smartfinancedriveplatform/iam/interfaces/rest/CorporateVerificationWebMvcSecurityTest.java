package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.UserCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.application.internal.queryservices.UserQueryService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ConfirmCorporateVerificationCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.InitiateCorporateVerificationCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationInitiated;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationResult;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.CorporateLookupQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.CorporateVerificationController;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CorporateLookupResource;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UsersController.class, CorporateVerificationController.class, GlobalExceptionHandler.class})
@Import(CorporateVerificationWebMvcSecurityTest.SecurityTestConfig.class)
@DisplayName("Corporate Verification WebMvc Security & SpEL Authorization Tests")
class CorporateVerificationWebMvcSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private UserQueryService userQueryService;

    @MockitoBean
    private CorporateLookupQueryService corporateLookupQueryService;

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

    // ==========================================
    // Corporate Lookup Endpoint Security
    // ==========================================

    @Test
    @DisplayName("GET /lookup/{ruc} should return 401 Unauthorized when unauthenticated")
    void lookupRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/partners/corporate-verification/lookup/20100047218"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /lookup/{ruc} should return 200 OK when authenticated")
    @WithMockUser(username = "any-user", roles = {"USER"})
    void lookupSucceedsWhenAuthenticated() throws Exception {
        var resource = new CorporateLookupResource(
                "20100047218",
                "FINANCIAL_INSTITUTION",
                "ROLE_FINANCIAL_INSTITUTION",
                "BANCO DE CREDITO DEL PERU",
                "CALLE CENTENARIO 156",
                "150101",
                List.of("viabcp.com", "bcp.com.pe"),
                "https://logo.png",
                true
        );

        when(corporateLookupQueryService.lookupByRuc("20100047218")).thenReturn(Optional.of(resource));

        mockMvc.perform(get("/api/v1/partners/corporate-verification/lookup/20100047218"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedName").value("BANCO DE CREDITO DEL PERU"))
                .andExpect(jsonPath("$.targetRole").value("ROLE_FINANCIAL_INSTITUTION"));
    }

    // ==========================================
    // Corporate Verification /me Security
    // ==========================================

    @Test
    @DisplayName("POST /me/corporate-verification/initiate should return 401 when unauthenticated")
    void meInitiateRequiresAuthentication() throws Exception {
        String payload = """
                {
                    "ruc": "20100047218",
                    "corporateEmail": "finanzas@viabcp.com"
                }
                """;
        mockMvc.perform(post("/api/v1/users/me/corporate-verification/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /me/corporate-verification/initiate should return 200 OK when authenticated")
    @WithMockUser(username = "1", roles = {"USER"})
    void meInitiateSucceedsWhenAuthenticated() throws Exception {
        var initiated = new CorporateVerificationInitiated("sess-1", true, "f***s@viabcp.com", 600);
        when(userCommandService.handle(any(InitiateCorporateVerificationCommand.class))).thenReturn(initiated);

        String payload = """
                {
                    "ruc": "20100047218",
                    "corporateEmail": "finanzas@viabcp.com"
                }
                """;
        mockMvc.perform(post("/api/v1/users/me/corporate-verification/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-1"))
                .andExpect(jsonPath("$.maskedEmail").value("f***s@viabcp.com"));
    }

    @Test
    @DisplayName("POST /me/corporate-verification/confirm should return 200 OK when authenticated")
    @WithMockUser(username = "1", roles = {"USER"})
    void meConfirmSucceedsWhenAuthenticated() throws Exception {
        var result = new CorporateVerificationResult(
                true, "FINANCIAL_INSTITUTION", "ROLE_FINANCIAL_INSTITUTION", "bank-1", "BANCO DE CREDITO DEL PERU", "Verification successful"
        );
        when(userCommandService.handle(any(ConfirmCorporateVerificationCommand.class))).thenReturn(result);

        String payload = """
                {
                    "ruc": "20100047218",
                    "code": "123456"
                }
                """;
        mockMvc.perform(post("/api/v1/users/me/corporate-verification/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.assignedRole").value("ROLE_FINANCIAL_INSTITUTION"));
    }

    // ==========================================
    // Corporate Verification /{userId} IDOR & RBAC Protection
    // ==========================================

    @Test
    @DisplayName("POST /{userId}/corporate-verification/initiate should return 403 Forbidden when user is not self or ADMIN")
    @WithMockUser(username = "user-2", roles = {"USER"})
    void initiateForbiddenWhenNotSelfOrAdmin() throws Exception {
        when(ownershipChecker.isUserSelf(eq(1L), any())).thenReturn(false);

        String payload = """
                {
                    "ruc": "20100047218",
                    "corporateEmail": "finanzas@viabcp.com"
                }
                """;
        mockMvc.perform(post("/api/v1/users/1/corporate-verification/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /{userId}/corporate-verification/initiate should return 200 OK when user is self")
    @WithMockUser(username = "user-1", roles = {"USER"})
    void initiateSucceedsWhenUserIsSelf() throws Exception {
        when(ownershipChecker.isUserSelf(eq(1L), any())).thenReturn(true);
        var initiated = new CorporateVerificationInitiated("sess-1", true, "f***s@viabcp.com", 600);
        when(userCommandService.handle(any(InitiateCorporateVerificationCommand.class))).thenReturn(initiated);

        String payload = """
                {
                    "ruc": "20100047218",
                    "corporateEmail": "finanzas@viabcp.com"
                }
                """;
        mockMvc.perform(post("/api/v1/users/1/corporate-verification/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-1"));
    }

    @Test
    @DisplayName("POST /{userId}/corporate-verification/confirm should return 403 Forbidden when user is not self or ADMIN")
    @WithMockUser(username = "user-2", roles = {"USER"})
    void confirmForbiddenWhenNotSelfOrAdmin() throws Exception {
        when(ownershipChecker.isUserSelf(eq(1L), any())).thenReturn(false);

        String payload = """
                {
                    "ruc": "20100047218",
                    "code": "123456"
                }
                """;
        mockMvc.perform(post("/api/v1/users/1/corporate-verification/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /{userId}/corporate-verification/confirm should return 200 OK when user is ADMIN")
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    void confirmSucceedsWhenAdmin() throws Exception {
        var result = new CorporateVerificationResult(
                true, "DEALERSHIP", "ROLE_DEALER", "autoland-1", "AUTOLAND S.A.", "Verification successful"
        );
        when(userCommandService.handle(any(ConfirmCorporateVerificationCommand.class))).thenReturn(result);

        String payload = """
                {
                    "ruc": "20100128056",
                    "code": "654321"
                }
                """;
        mockMvc.perform(post("/api/v1/users/1/corporate-verification/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.assignedRole").value("ROLE_DEALER"));
    }
}

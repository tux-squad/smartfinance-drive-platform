package com.smartfinance.smartfinancedriveplatform.system;

import com.smartfinance.smartfinancedriveplatform.analytics.application.queryservices.AnalyticsQueryService;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.*;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.AnalyticsController;
import com.smartfinance.smartfinancedriveplatform.catalog.application.commandservices.VehicleCommandService;
import com.smartfinance.smartfinancedriveplatform.catalog.application.outboundservices.storage.VehicleImageStorageService;
import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.commands.CreateVehicleCommand;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetAllVehiclesQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehicleByIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.catalog.interfaces.rest.VehiclesController;
import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.CreditApplicationCommandService;
import com.smartfinance.smartfinancedriveplatform.financing.application.commandservices.SimulationCommandService;
import com.smartfinance.smartfinancedriveplatform.financing.application.queryservices.CreditApplicationQueryService;
import com.smartfinance.smartfinancedriveplatform.financing.application.queryservices.SimulationQueryService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.Simulation;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.CreateSimulationCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.commands.UpdateCreditApplicationStatusCommand;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.CreditApplicationId;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.VehicleInsuranceType;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.CreditApplicationsController;
import com.smartfinance.smartfinancedriveplatform.financing.interfaces.rest.SimulationsController;
import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.UserCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.application.internal.queryservices.UserQueryService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignInCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.SignUpCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.UpdateUserRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.UserCommandService.AuthenticationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.JwtTokenService;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.services.TokenBlacklistService;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.AuthenticationController;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.UsersController;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.scoring.application.commandservices.CreditScoreCommandService;
import com.smartfinance.smartfinancedriveplatform.scoring.application.queryservices.CreditScoreQueryService;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.aggregates.CreditScore;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.commands.EvaluateCreditScoreCommand;
import com.smartfinance.smartfinancedriveplatform.scoring.interfaces.rest.CreditScoresController;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.OwnershipChecker;
import com.smartfinance.smartfinancedriveplatform.shared.interfaces.rest.setup.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Acceptance and System Test Suite covering ST-01 to ST-18.
 */
@WebMvcTest(controllers = {
        AuthenticationController.class,
        UsersController.class,
        VehiclesController.class,
        CreditScoresController.class,
        SimulationsController.class,
        CreditApplicationsController.class,
        AnalyticsController.class,
        GlobalExceptionHandler.class
})
@Import(SystemAcceptanceTests.SecurityTestConfig.class)
@DisplayName("Suite de Pruebas Unitarias del Sistema (ST-01 a ST-18)")
public class SystemAcceptanceTests {

    @Autowired
    private MockMvc mockMvc;

    // --- Mockito Beans for IAM & Auth ---
    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private UserQueryService userQueryService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    // --- Mockito Beans for Catalog ---
    @MockitoBean
    private VehicleCommandService vehicleCommandService;

    @MockitoBean
    private VehicleQueryService vehicleQueryService;

    @MockitoBean
    private VehicleImageStorageService vehicleImageStorageService;

    // --- Mockito Beans for Scoring & Financing ---
    @MockitoBean
    private CreditScoreCommandService creditScoreCommandService;

    @MockitoBean
    private CreditScoreQueryService creditScoreQueryService;

    @MockitoBean
    private SimulationCommandService simulationCommandService;

    @MockitoBean
    private SimulationQueryService simulationQueryService;

    @MockitoBean
    private CreditApplicationCommandService creditApplicationCommandService;

    @MockitoBean
    private CreditApplicationQueryService creditApplicationQueryService;

    // --- Mockito Beans for Analytics & Partners ---
    @MockitoBean
    private AnalyticsQueryService analyticsQueryService;

    @MockitoBean
    private FinancialEntityQueryService financialEntityQueryService;

    @MockitoBean(name = "ownershipChecker")
    private OwnershipChecker ownershipChecker;

    // --- Infrastructure Security Filters ---
    @MockitoBean
    private com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline.RateLimitingFilter rateLimitingFilter;

    @MockitoBean
    private org.springframework.cache.CacheManager cacheManager;

    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .authorizeHttpRequests(auth -> {
                        auth.requestMatchers("/api/v1/auth/**").permitAll();
                        auth.requestMatchers(HttpMethod.GET, "/api/v1/vehicles/my-listings").authenticated();
                        auth.requestMatchers(HttpMethod.GET, "/api/v1/vehicles", "/api/v1/vehicles/**").permitAll();
                        auth.anyRequest().authenticated();
                    });
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

    // =========================================================================
    // 1. Gestión de Cuentas y Sesión (ST-01 a ST-05)
    // =========================================================================
    @Nested
    @DisplayName("1. Gestión de Cuentas y Sesión")
    class AccountAndSessionTests {

        @Test
        @DisplayName("ST-01 (US-01): Registro exitoso (POST /api/v1/auth/registrations) con datos válidos")
        void st01_registerSuccessWithValidData() throws Exception {
            User mockUser = new User(1L, new Username("nuevo.usuario@smartfinance.pe"), new Password("hashedPassword123!"), List.of(Roles.ROLE_USER));
            when(userCommandService.handle(any(SignUpCommand.class))).thenReturn(Optional.of(mockUser));

            String payload = """
                    {
                        "username": "nuevo.usuario@smartfinance.pe",
                        "password": "Password123!",
                        "roles": ["ROLE_USER"]
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/registrations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("nuevo.usuario@smartfinance.pe"));
        }

        @Test
        @DisplayName("ST-02 (US-01): Registro con datos inválidos (correo mal formado). Debe dar error 400")
        void st02_registerFailsWithInvalidEmail() throws Exception {
            String payload = """
                    {
                        "username": "correo-no-valido-sin-arroba",
                        "password": "Password123!"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/registrations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("ST-03 (US-02): Inicio de sesión exitoso (POST /api/v1/auth/sessions). Debe devolver los tokens")
        void st03_signInSuccessReturnsTokens() throws Exception {
            User mockUser = new User(1L, new Username("usuario@smartfinance.pe"), new Password("hashedPassword123!"), List.of(Roles.ROLE_USER));
            AuthenticationResult authResult = new AuthenticationResult(mockUser, "valid-access-jwt-token", "valid-refresh-token");
            when(userCommandService.handle(any(SignInCommand.class))).thenReturn(Optional.of(authResult));

            String payload = """
                    {
                        "username": "usuario@smartfinance.pe",
                        "password": "Password123!"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("valid-access-jwt-token"))
                    .andExpect(jsonPath("$.refreshToken").value("valid-refresh-token"))
                    .andExpect(jsonPath("$.username").value("usuario@smartfinance.pe"));
        }

        @Test
        @DisplayName("ST-04 (US-03): Cierre de sesión (DELETE /api/v1/auth/sessions/current)")
        void st04_signOutSuccess() throws Exception {
            when(jwtTokenService.validateToken("test-jwt-token")).thenReturn(true);
            when(jwtTokenService.getJtiFromToken("test-jwt-token")).thenReturn("jti-123");

            mockMvc.perform(delete("/api/v1/auth/sessions/current")
                            .header("Authorization", "Bearer test-jwt-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User signed out successfully"));
        }

        @Test
        @DisplayName("ST-05 (US-03): Intentar usar la sesión anterior tras cerrar sesión o sin autenticar. Debe dar error 401")
        void st05_accessProtectedEndpointWithoutValidSessionFails401() throws Exception {
            // Invoking a protected authenticated endpoint without Bearer token
            mockMvc.perform(get("/api/v1/vehicles/my-listings"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // 2. Gestión de Roles (Administrador) (ST-06, ST-07)
    // =========================================================================
    @Nested
    @DisplayName("2. Gestión de Roles (Administrador)")
    class RoleManagementTests {

        @Test
        @DisplayName("ST-06 (US-08): Modificar roles siendo administrador (PUT /api/v1/users/{userId}/roles)")
        @WithMockUser(username = "admin-user", roles = {"ADMIN"})
        void st06_updateUserRolesAsAdminSucceeds() throws Exception {
            User updatedUser = new User(2L, new Username("cliente@smartfinance.pe"), new Password("Password123!"), List.of(Roles.ROLE_DEALER));
            when(userCommandService.handle(any(UpdateUserRoleCommand.class))).thenReturn(Optional.of(updatedUser));

            String payload = """
                    {
                        "role": "ROLE_DEALER"
                    }
                    """;

            mockMvc.perform(put("/api/v1/users/2/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.roles[0]").value("ROLE_DEALER"));
        }

        @Test
        @DisplayName("ST-07 (US-08): Intentar modificar roles siendo usuario normal. Debe dar error 403")
        @WithMockUser(username = "user-normal", roles = {"USER"})
        void st07_updateUserRolesAsNormalUserFails403() throws Exception {
            String payload = """
                    {
                        "role": "ROLE_ADMIN"
                    }
                    """;

            mockMvc.perform(put("/api/v1/users/2/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // 3. Catálogo e Inventario de Vehículos (ST-08 a ST-12)
    // =========================================================================
    @Nested
    @DisplayName("3. Catálogo e Inventario de Vehículos")
    class CatalogAndInventoryTests {

        @Test
        @DisplayName("ST-08 (US-16): Publicar un vehículo con rol de concesionaria (POST /api/v1/vehicles)")
        @WithMockUser(username = "dealer-1", roles = {"DEALER"})
        void st08_publishVehicleAsDealerSucceeds() throws Exception {
            UUID vehicleId = UUID.randomUUID();
            UUID entityId = UUID.randomUUID();
            Vehicle vehicle = new Vehicle(
                    new VehicleId(vehicleId),
                    new UserId("dealer-1"),
                    new FinancialEntityId(entityId),
                    "Toyota",
                    "Corolla",
                    2024,
                    "NEW",
                    new Money(new BigDecimal("22500.00"), "USD"),
                    "https://cloudinary.com/toyota.png",
                    "ACTIVE",
                    0,
                    "AUTOMATIC",
                    "2.0L",
                    "FWD",
                    List.of("https://cloudinary.com/toyota.png")
            );

            when(vehicleCommandService.handle(any(CreateVehicleCommand.class))).thenReturn(Optional.of(vehicle));

            String payload = String.format("""
                    {
                        "userId": "dealer-1",
                        "financialEntityId": "%s",
                        "brand": "Toyota",
                        "model": "Corolla",
                        "manufactureYear": 2024,
                        "condition": "NEW",
                        "priceAmount": 22500.00,
                        "currency": "USD",
                        "imagePath": "https://cloudinary.com/toyota.png"
                    }
                    """, entityId);

            mockMvc.perform(post("/api/v1/vehicles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.brand").value("Toyota"))
                    .andExpect(jsonPath("$.model").value("Corolla"))
                    .andExpect(jsonPath("$.manufactureYear").value(2024));
        }

        @Test
        @DisplayName("ST-09 (US-16): Intentar publicar un vehículo con datos inválidos (año 1899). Debe dar error 400")
        @WithMockUser(username = "dealer-1", roles = {"DEALER"})
        void st09_publishVehicleWithInvalidYearFails400() throws Exception {
            String payload = """
                    {
                        "userId": "dealer-1",
                        "financialEntityId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
                        "brand": "Toyota",
                        "model": "Corolla",
                        "manufactureYear": 1899,
                        "condition": "NEW",
                        "priceAmount": 20000.00,
                        "currency": "USD"
                    }
                    """;

            mockMvc.perform(post("/api/v1/vehicles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("ST-10 (US-23): Buscar vehículos aplicando filtros en la URL (GET /api/v1/vehicles?brand=Toyota&minPrice=10000...)")
        void st10_searchVehiclesWithFiltersSucceeds() throws Exception {
            Vehicle vehicle = new Vehicle(
                    new VehicleId(UUID.randomUUID()),
                    new UserId("dealer-1"),
                    new FinancialEntityId(UUID.randomUUID()),
                    "Toyota",
                    "Yaris",
                    2023,
                    "NEW",
                    new Money(new BigDecimal("18000.00"), "USD"),
                    "https://img.png",
                    "ACTIVE",
                    0,
                    "MANUAL",
                    "1.5L",
                    "FWD",
                    List.of("https://img.png")
            );

            when(vehicleQueryService.handle(any(GetAllVehiclesQuery.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(vehicle)));

            mockMvc.perform(get("/api/v1/vehicles")
                            .param("brand", "Toyota")
                            .param("minPrice", "10000")
                            .param("maxPrice", "30000")
                            .param("condition", "NEW"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                    .andExpect(jsonPath("$.content[0].model").value("Yaris"));
        }

        @Test
        @DisplayName("ST-11 (US-25): Ver el detalle de un vehículo específico (GET /api/v1/vehicles/{vehicleId})")
        void st11_getVehicleByIdSucceeds() throws Exception {
            UUID vehicleId = UUID.randomUUID();
            Vehicle vehicle = new Vehicle(
                    new VehicleId(vehicleId),
                    new UserId("dealer-1"),
                    new FinancialEntityId(UUID.randomUUID()),
                    "Nissan",
                    "Sentra",
                    2023,
                    "USED",
                    new Money(new BigDecimal("19500.00"), "USD"),
                    "https://nissan.png",
                    "ACTIVE",
                    15000,
                    "CVT",
                    "2.0L",
                    "FWD",
                    List.of("https://nissan.png")
            );

            when(vehicleQueryService.handle(any(GetVehicleByIdQuery.class))).thenReturn(Optional.of(vehicle));

            mockMvc.perform(get("/api/v1/vehicles/" + vehicleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.brand").value("Nissan"))
                    .andExpect(jsonPath("$.model").value("Sentra"))
                    .andExpect(jsonPath("$.priceAmount").value(19500.00));
        }

        @Test
        @DisplayName("ST-12 (US-18): Intentar cambiar el estado de un vehículo que pertenece a otra concesionaria. Debe dar error 403")
        @WithMockUser(username = "dealer-rival", roles = {"DEALER"})
        void st12_updateVehicleStatusOtherDealerFails403() throws Exception {
            UUID vehicleId = UUID.randomUUID();
            // Ownership check explicitly fails because current user does not own the vehicle
            when(ownershipChecker.isVehicleOwner(eq(vehicleId), any())).thenReturn(false);

            String payload = """
                    {
                        "status": "SOLD"
                    }
                    """;

            mockMvc.perform(patch("/api/v1/vehicles/" + vehicleId + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // 4. Riesgo, Simulaciones y Solicitudes (ST-13 a ST-17)
    // =========================================================================
    @Nested
    @DisplayName("4. Riesgo, Simulaciones y Solicitudes")
    class RiskSimulationsAndApplicationsTests {

        @Test
        @DisplayName("ST-13 (US-28): Realizar pre-evaluación de riesgo crediticio (POST /api/v1/credit-scores)")
        @WithMockUser(username = "cliente-1", roles = {"USER"})
        void st13_evaluateCreditScoreSucceeds() throws Exception {
            CreditScore mockScore = new CreditScore(
                    "profile-1",
                    "sim-1",
                    new Money(new BigDecimal("5000.00"), "PEN"),
                    new Money(new BigDecimal("1200.00"), "PEN")
            );

            when(creditScoreCommandService.handle(any(EvaluateCreditScoreCommand.class))).thenReturn(Optional.of(mockScore));

            String payload = """
                    {
                        "profileId": "profile-1",
                        "simulationId": "sim-1",
                        "monthlyIncomeAmount": 5000.00,
                        "projectedMonthlyInstallmentAmount": 1200.00,
                        "currency": "PEN"
                    }
                    """;

            mockMvc.perform(post("/api/v1/credit-scores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.profileId").value("profile-1"))
                    .andExpect(jsonPath("$.riskTier").isNotEmpty());
        }

        @Test
        @DisplayName("ST-14 (US-29): Crear una simulación de crédito vehicular (POST /api/v1/simulations)")
        @WithMockUser(username = "cliente-1", roles = {"USER"})
        void st14_createSimulationSucceeds() throws Exception {
            Simulation mockSimulation = new Simulation(
                    "Plan Mi Auto Nuevo",
                    "cliente-1",
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    new Money(new BigDecimal("20000.00"), "USD"),
                    Percent.of(20.0),
                    Percent.of(0.0),
                    Percent.of(12.5),
                    Percent.of(0.05),
                    new Money(new BigDecimal("50.00"), "USD"),
                    VehicleInsuranceType.ENDOSADO,
                    36,
                    GracePeriodType.NONE,
                    0,
                    Money.zero("USD"),
                    Percent.of(10.0),
                    LocalDate.now()
            );

            when(simulationCommandService.handle(any(CreateSimulationCommand.class))).thenReturn(Optional.of(mockSimulation));

            String payload = """
                    {
                        "title": "Plan Mi Auto Nuevo",
                        "userId": "cliente-1",
                        "vehicleId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
                        "financialEntityId": "f1e2d3c4-b5a6-9788-0011-223344556677",
                        "vehiclePriceAmount": 20000.00,
                        "currency": "USD",
                        "downPaymentPercentage": 20.0,
                        "balloonPaymentPercentage": 0.0,
                        "annualEffectiveRate": 12.5,
                        "monthlyCreditLifeInsuranceRate": 0.05,
                        "vehicleInsuranceFeeAmount": 50.00,
                        "vehicleInsuranceType": "ENDOSADO",
                        "loanTermMonths": 36,
                        "gracePeriodType": "NONE",
                        "gracePeriodMonths": 0,
                        "initialFeesAmount": 0.00,
                        "discountRate": 10.0
                    }
                    """;

            mockMvc.perform(post("/api/v1/simulations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Plan Mi Auto Nuevo"))
                    .andExpect(jsonPath("$.loanTermMonths").value(36));
        }

        @Test
        @DisplayName("ST-15 (US-32): Intentar eliminar la simulación de otro cliente. Debe dar error 403")
        @WithMockUser(username = "cliente-rival", roles = {"USER"})
        void st15_deleteSimulationOtherUserFails403() throws Exception {
            UUID simulationId = UUID.randomUUID();
            when(ownershipChecker.isSimulationOwner(eq(simulationId), any())).thenReturn(false);

            mockMvc.perform(delete("/api/v1/simulations/" + simulationId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ST-16 (US-36): Intentar evaluar una solicitud de crédito sin ser Entidad Financiera. Debe dar error 403")
        @WithMockUser(username = "cliente-normal", roles = {"USER"})
        void st16_updateApplicationStatusAsNormalUserFails403() throws Exception {
            UUID applicationId = UUID.randomUUID();
            String payload = """
                    {
                        "status": "PRE_APPROVED",
                        "notes": "Aprobado preliminarmente"
                    }
                    """;

            mockMvc.perform(patch("/api/v1/credit-applications/" + applicationId + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ST-17 (US-36): Registrar la evaluación (Aprobar/Rechazar) siendo Entidad Financiera")
        @WithMockUser(username = "banco-oficial", roles = {"FINANCIAL_INSTITUTION"})
        void st17_updateApplicationStatusAsFinancialInstitutionSucceeds() throws Exception {
            UUID applicationId = UUID.randomUUID();
            CreditApplication mockApplication = new CreditApplication(
                    new CreditApplicationId(applicationId),
                    "cliente-1",
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new Money(new BigDecimal("16000.00"), "USD"),
                    new Money(new BigDecimal("4000.00"), "USD"),
                    36,
                    new Money(new BigDecimal("3500.00"), "USD"),
                    "EMPLOYED",
                    "PRE_APPROVED",
                    "Aprobado por comité de riesgos"
            );

            when(creditApplicationCommandService.handle(any(UpdateCreditApplicationStatusCommand.class)))
                    .thenReturn(Optional.of(mockApplication));

            String payload = """
                    {
                        "status": "PRE_APPROVED",
                        "notes": "Aprobado por comité de riesgos"
                    }
                    """;

            mockMvc.perform(patch("/api/v1/credit-applications/" + applicationId + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PRE_APPROVED"))
                    .andExpect(jsonPath("$.notes").value("Aprobado por comité de riesgos"));
        }
    }

    // =========================================================================
    // 5. Estadísticas (ST-18)
    // =========================================================================
    @Nested
    @DisplayName("5. Estadísticas")
    class AnalyticsTests {

        @Test
        @DisplayName("ST-18 (US-21): Consultar métricas propias como concesionaria (GET /api/v1/analytics/dealer)")
        @WithMockUser(username = "dealer-1", roles = {"DEALER"})
        void st18_getDealerMetricsAsDealerSucceeds() throws Exception {
            DealerDashboardMetrics metrics = new DealerDashboardMetrics(
                    "dealer-1",
                    new DealerInventoryMetrics(8, 6, 2, 0, BigDecimal.valueOf(180000), BigDecimal.ZERO),
                    new DealerCrmMetrics(12, 4, 3, 2, 1, 2, 0, 25.0),
                    new DealerTestDriveMetrics(4, 2, 1, 1, 0),
                    new DealerFinancingMetrics(5, 3, 1, 1),
                    "ALL_TIME"
            );

            when(analyticsQueryService.handle(any(GetDealerDashboardMetricsQuery.class))).thenReturn(metrics);

            mockMvc.perform(get("/api/v1/analytics/dealer")
                            .param("period", "ALL_TIME"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dealerUserId").value("dealer-1"))
                    .andExpect(jsonPath("$.inventory.totalVehicles").value(8))
                    .andExpect(jsonPath("$.crm.totalLeads").value(12));
        }
    }
}

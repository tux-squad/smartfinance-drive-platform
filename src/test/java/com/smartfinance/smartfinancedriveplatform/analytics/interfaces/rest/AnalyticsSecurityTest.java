package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.analytics.application.queryservices.AnalyticsQueryService;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetAdminDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetFinancialInstitutionDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.*;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.AdminDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.DealerDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.FinancialInstitutionDashboardResource;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.OwnershipChecker;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Analytics Security & IDOR Authorization Tests")
class AnalyticsSecurityTest {

    @Mock
    private AnalyticsQueryService analyticsQueryService;

    @Mock
    private FinancialEntityQueryService financialEntityQueryService;

    @Mock
    private OwnershipChecker ownershipChecker;

    @InjectMocks
    private AnalyticsController analyticsController;

    private final String dealerUserId = "dealer-100";
    private final String attackerDealerUserId = "dealer-attacker";
    private final UUID bankEntityId = UUID.randomUUID();
    private final UUID otherBankEntityId = UUID.randomUUID();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String userId, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, "credentials", List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails(userId, userId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("DEALER should access their own dashboard metrics when omitting dealerUserId")
    void dealerCanAccessOwnMetricsWhenOmitted() {
        authenticateAs(dealerUserId, "DEALER");

        DealerDashboardMetrics metrics = createSampleDealerMetrics(dealerUserId);
        when(analyticsQueryService.handle(any(GetDealerDashboardMetricsQuery.class))).thenReturn(metrics);

        ResponseEntity<DealerDashboardResource> response = analyticsController.getDealerMetrics(null, "ALL_TIME");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(dealerUserId, response.getBody().dealerUserId());
    }

    @Test
    @DisplayName("DEALER should access their own dashboard metrics when explicitly providing their own ID")
    void dealerCanAccessOwnMetricsWithOwnId() {
        authenticateAs(dealerUserId, "DEALER");

        DealerDashboardMetrics metrics = createSampleDealerMetrics(dealerUserId);
        when(analyticsQueryService.handle(any(GetDealerDashboardMetricsQuery.class))).thenReturn(metrics);

        ResponseEntity<DealerDashboardResource> response = analyticsController.getDealerMetrics(dealerUserId, "ALL_TIME");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(dealerUserId, response.getBody().dealerUserId());
    }

    @Test
    @DisplayName("DEALER should be blocked with 403 AccessDeniedException when attempting IDOR to read another dealer's data")
    void dealerCannotAccessOtherDealerMetrics_IdorBlocked() {
        authenticateAs(attackerDealerUserId, "DEALER");

        assertThrows(AccessDeniedException.class, () ->
                analyticsController.getDealerMetrics(dealerUserId, "ALL_TIME"));
    }

    @Test
    @DisplayName("DEALER providing blank or empty string for dealerUserId safely falls back to own dealership")
    void dealerCanAccessOwnMetricsWithBlankDealerUserId() {
        authenticateAs(dealerUserId, "DEALER");

        DealerDashboardMetrics metrics = createSampleDealerMetrics(dealerUserId);
        when(analyticsQueryService.handle(any(GetDealerDashboardMetricsQuery.class))).thenReturn(metrics);

        ResponseEntity<DealerDashboardResource> response = analyticsController.getDealerMetrics("   ", "ALL_TIME");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(dealerUserId, response.getBody().dealerUserId());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when period parameter is invalid")
    void dealerThrowsIllegalArgumentExceptionOnInvalidPeriod() {
        authenticateAs(dealerUserId, "DEALER");

        assertThrows(IllegalArgumentException.class, () ->
                analyticsController.getDealerMetrics(dealerUserId, "INVALID_PERIOD_99"));
    }

    @Test
    @DisplayName("FINANCIAL_INSTITUTION should auto-resolve their bank entity ID when omitted")
    void financialInstitutionCanAccessOwnMetricsWhenOmitted() {
        authenticateAs("bank-user-1", "FINANCIAL_INSTITUTION");

        FinancialEntity entity = new FinancialEntity(
                new FinancialEntityId(bankEntityId),
                "bank-user-1",
                "Banco BCP",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityQueryService.handle(new GetFinancialEntityByUserIdQuery("bank-user-1")))
                .thenReturn(Optional.of(entity));

        FinancialInstitutionDashboardMetrics metrics = createSampleBankMetrics(bankEntityId, "Banco BCP");
        when(analyticsQueryService.handle(new GetFinancialInstitutionDashboardMetricsQuery(bankEntityId)))
                .thenReturn(Optional.of(metrics));

        ResponseEntity<FinancialInstitutionDashboardResource> response =
                analyticsController.getFinancialInstitutionMetrics(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(bankEntityId, response.getBody().financialEntityId());
    }

    @Test
    @DisplayName("FINANCIAL_INSTITUTION should be rejected when no financial entity is associated with their account")
    void financialInstitutionRejectedWhenNoEntityAssociated() {
        authenticateAs("unlinked-bank-user", "FINANCIAL_INSTITUTION");

        when(financialEntityQueryService.handle(new GetFinancialEntityByUserIdQuery("unlinked-bank-user")))
                .thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                analyticsController.getFinancialInstitutionMetrics(null)
        );

        assertEquals("partners.error.financialEntity.notAssociated", exception.getMessage());
    }

    @Test
    @DisplayName("IDOR prevention: FINANCIAL_INSTITUTION cannot query another bank's entity ID")
    void idorPreventionFinancialInstitutionCannotAccessOtherBank() {
        authenticateAs("bank-user-attacker", "FINANCIAL_INSTITUTION");

        // Attacker attempts to read otherBankEntityId, ownershipChecker returns false
        when(ownershipChecker.isFinancialEntityOwner(eq(otherBankEntityId), any())).thenReturn(false);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                analyticsController.getFinancialInstitutionMetrics(otherBankEntityId)
        );

        assertEquals("partners.error.accessDenied.notOwner", exception.getMessage());
    }

    @Test
    @DisplayName("ADMIN role can access any financial institution dashboard")
    void adminCanAccessAnyFinancialInstitution() {
        authenticateAs("admin-user", "ADMIN");

        FinancialInstitutionDashboardMetrics metrics = createSampleBankMetrics(otherBankEntityId, "Interbank");
        when(analyticsQueryService.handle(new GetFinancialInstitutionDashboardMetricsQuery(otherBankEntityId)))
                .thenReturn(Optional.of(metrics));

        ResponseEntity<FinancialInstitutionDashboardResource> response =
                analyticsController.getFinancialInstitutionMetrics(otherBankEntityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(otherBankEntityId, response.getBody().financialEntityId());
    }

    @Test
    @DisplayName("ADMIN can access platform-wide admin metrics")
    void adminCanAccessPlatformWideMetrics() {
        authenticateAs("admin-user", "ADMIN");

        AdminDashboardMetrics metrics = new AdminDashboardMetrics(
                10, 8, 4, 120, 250, 40, 90, 15, BigDecimal.valueOf(3500.00)
        );
        when(analyticsQueryService.handle(any(GetAdminDashboardMetricsQuery.class))).thenReturn(metrics);

        ResponseEntity<AdminDashboardResource> response = analyticsController.getAdminMetrics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10, response.getBody().totalDealerships());
    }

    // Helper builders
    private DealerDashboardMetrics createSampleDealerMetrics(String userId) {
        return new DealerDashboardMetrics(
                userId,
                new DealerInventoryMetrics(5, 4, 1, 0, BigDecimal.valueOf(100000), BigDecimal.ZERO),
                new DealerCrmMetrics(10, 3, 2, 2, 1, 2, 0, 20.0),
                new DealerTestDriveMetrics(3, 1, 1, 1, 0),
                new DealerFinancingMetrics(4, 2, 1, 1),
                "ALL_TIME"
        );
    }

    private FinancialInstitutionDashboardMetrics createSampleBankMetrics(UUID id, String name) {
        return new FinancialInstitutionDashboardMetrics(
                id,
                name,
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
}

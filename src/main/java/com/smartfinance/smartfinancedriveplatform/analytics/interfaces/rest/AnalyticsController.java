package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.analytics.application.queryservices.AnalyticsQueryService;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetAdminDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetFinancialInstitutionDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.MetricPeriod;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.AdminDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.DealerDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.FinancialInstitutionDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.transform.AdminDashboardResourceFromModelAssembler;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.transform.DealerDashboardResourceFromModelAssembler;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.transform.FinancialInstitutionDashboardResourceFromModelAssembler;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.OwnershipChecker;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for unified Dashboard Analytics across Dealer, Financial Institution, and Admin portals.
 * Fully secured against IDOR using SpEL @ownershipChecker and contextual tenant enforcement.
 */
@RestController
@RequestMapping(value = "/api/v1/analytics", produces = "application/json")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;
    private final FinancialEntityQueryService financialEntityQueryService;
    private final OwnershipChecker ownershipChecker;

    public AnalyticsController(AnalyticsQueryService analyticsQueryService,
                               FinancialEntityQueryService financialEntityQueryService,
                               OwnershipChecker ownershipChecker) {
        this.analyticsQueryService = analyticsQueryService;
        this.financialEntityQueryService = financialEntityQueryService;
        this.ownershipChecker = ownershipChecker;
    }

    /**
     * GET /api/v1/analytics/dealer
     * Aggregated factual metrics for Dealerships (Inventory, CRM Leads, Test Drives, Credit applications).
     * Protected against IDOR: non-admins can strictly only view their own dealership data.
     */
    @GetMapping("/dealer")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DEALER') and (#dealerUserId == null or #dealerUserId.isBlank() or @ownershipChecker.isUserSelfStr(#dealerUserId, authentication)))")
    public ResponseEntity<DealerDashboardResource> getDealerMetrics(
            @RequestParam(required = false) String dealerUserId,
            @RequestParam(defaultValue = "ALL_TIME") String period) {

        String currentUserId = SecurityUtils.getRequiredCurrentUserId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && dealerUserId != null && !dealerUserId.isBlank() && !dealerUserId.equals(currentUserId)) {
            throw new AccessDeniedException("iam.error.accessDenied.notOwner");
        }

        String targetDealerId = (isAdmin && dealerUserId != null && !dealerUserId.isBlank())
                ? dealerUserId
                : currentUserId;

        MetricPeriod metricPeriod;
        if (period == null || period.isBlank()) {
            metricPeriod = MetricPeriod.ALL_TIME;
        } else {
            try {
                metricPeriod = MetricPeriod.valueOf(period.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("analytics.error.invalidPeriod: " + period);
            }
        }

        var metrics = analyticsQueryService.handle(new GetDealerDashboardMetricsQuery(targetDealerId, metricPeriod));
        return ResponseEntity.ok(DealerDashboardResourceFromModelAssembler.toResource(metrics));
    }

    /**
     * GET /api/v1/analytics/financial-institution?financialEntityId={uuid}
     * Aggregated metrics for Financial Entities.
     * Protected against IDOR: non-admins can strictly only view their own financial entity data.
     */
    @GetMapping("/financial-institution")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('FINANCIAL_INSTITUTION') and (#financialEntityId == null or @ownershipChecker.isFinancialEntityOwner(#financialEntityId, authentication)))")
    public ResponseEntity<FinancialInstitutionDashboardResource> getFinancialInstitutionMetrics(
            @RequestParam(required = false) UUID financialEntityId) {

        String currentUserId = SecurityUtils.getRequiredCurrentUserId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        UUID targetEntityId = financialEntityId;
        if (targetEntityId == null) {
            targetEntityId = financialEntityQueryService.handle(new GetFinancialEntityByUserIdQuery(currentUserId))
                    .map(e -> e.getId().value())
                    .orElseThrow(() -> new AccessDeniedException("partners.error.financialEntity.notAssociated"));
        } else if (!isAdmin && !ownershipChecker.isFinancialEntityOwner(targetEntityId, auth)) {
            throw new AccessDeniedException("partners.error.accessDenied.notOwner");
        }

        return analyticsQueryService.handle(new GetFinancialInstitutionDashboardMetricsQuery(targetEntityId))
                .map(FinancialInstitutionDashboardResourceFromModelAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/analytics/admin
     * Platform-wide aggregated metrics for Administrators.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResource> getAdminMetrics() {
        var metrics = analyticsQueryService.handle(new GetAdminDashboardMetricsQuery());
        return ResponseEntity.ok(AdminDashboardResourceFromModelAssembler.toResource(metrics));
    }
}

package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.analytics.application.queryservices.AnalyticsQueryService;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetAdminDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetFinancialInstitutionDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.AdminDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.DealerDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.FinancialInstitutionDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.transform.AdminDashboardResourceFromModelAssembler;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.transform.DealerDashboardResourceFromModelAssembler;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.transform.FinancialInstitutionDashboardResourceFromModelAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for unified Dashboard Analytics across Dealer, Financial Institution, and Admin portals.
 */
@RestController
@RequestMapping(value = "/api/v1/analytics", produces = "application/json")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    public AnalyticsController(AnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }

    /**
     * GET /api/v1/analytics/dealer
     * Aggregated metrics for Dealerships (Inventory, CRM Leads, Test Drives, Credit applications).
     */
    @GetMapping("/dealer")
    @PreAuthorize("hasAnyRole('DEALER', 'ADMIN')")
    public ResponseEntity<DealerDashboardResource> getDealerMetrics(
            @RequestParam(required = false) String dealerUserId) {

        String targetDealerId = (dealerUserId != null && !dealerUserId.isBlank())
                ? dealerUserId
                : SecurityUtils.getRequiredCurrentUserId();

        var metrics = analyticsQueryService.handle(new GetDealerDashboardMetricsQuery(targetDealerId));
        return ResponseEntity.ok(DealerDashboardResourceFromModelAssembler.toResource(metrics));
    }

    /**
     * GET /api/v1/analytics/financial-institution?financialEntityId={uuid}
     * Aggregated metrics for Financial Entities (Applications pipeline, Approval rate, Disbursed volume, TEA benchmark, Simulations).
     */
    @GetMapping("/financial-institution")
    @PreAuthorize("hasAnyRole('FINANCIAL_INSTITUTION', 'ADMIN')")
    public ResponseEntity<FinancialInstitutionDashboardResource> getFinancialInstitutionMetrics(
            @RequestParam UUID financialEntityId) {

        return analyticsQueryService.handle(new GetFinancialInstitutionDashboardMetricsQuery(financialEntityId))
                .map(FinancialInstitutionDashboardResourceFromModelAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/analytics/admin
     * Platform-wide aggregated metrics for Administrators (Users, Dealerships, Banks, Catalog, Subscriptions, MRR).
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResource> getAdminMetrics() {
        var metrics = analyticsQueryService.handle(new GetAdminDashboardMetricsQuery());
        return ResponseEntity.ok(AdminDashboardResourceFromModelAssembler.toResource(metrics));
    }
}

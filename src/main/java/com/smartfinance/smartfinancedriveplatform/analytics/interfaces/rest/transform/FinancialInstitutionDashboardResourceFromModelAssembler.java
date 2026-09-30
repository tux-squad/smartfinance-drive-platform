package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.FinancialInstitutionDashboardMetrics;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.FinancialInstitutionDashboardResource;

public final class FinancialInstitutionDashboardResourceFromModelAssembler {

    private FinancialInstitutionDashboardResourceFromModelAssembler() {}

    public static FinancialInstitutionDashboardResource toResource(FinancialInstitutionDashboardMetrics model) {
        if (model == null) return null;

        return new FinancialInstitutionDashboardResource(
                model.financialEntityId(),
                model.financialEntityName(),
                model.totalApplicationsReceived(),
                model.underReviewApplications(),
                model.approvedApplications(),
                model.rejectedApplications(),
                model.disbursedApplications(),
                model.approvalRate(),
                model.totalRequestedVolumePen(),
                model.totalDisbursedVolumePen(),
                model.averageTea(),
                model.activeRateBenchmarksCount()
        );
    }
}

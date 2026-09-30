package com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.DealerDashboardMetrics;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.DealerCrmMetricsResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.DealerDashboardResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.DealerFinancingMetricsResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.DealerInventoryMetricsResource;
import com.smartfinance.smartfinancedriveplatform.analytics.interfaces.rest.resources.DealerTestDriveMetricsResource;

public final class DealerDashboardResourceFromModelAssembler {

    private DealerDashboardResourceFromModelAssembler() {}

    public static DealerDashboardResource toResource(DealerDashboardMetrics model) {
        if (model == null) return null;

        DealerInventoryMetricsResource inventory = model.inventory() == null ? null : new DealerInventoryMetricsResource(
                model.inventory().totalVehicles(),
                model.inventory().availableVehicles(),
                model.inventory().reservedVehicles(),
                model.inventory().soldVehicles(),
                model.inventory().totalInventoryValuePen(),
                model.inventory().totalInventoryValueUsd()
        );

        DealerCrmMetricsResource crm = model.crm() == null ? null : new DealerCrmMetricsResource(
                model.crm().totalLeads(),
                model.crm().newLeads(),
                model.crm().contactedLeads(),
                model.crm().qualifiedLeads(),
                model.crm().inNegotiationLeads(),
                model.crm().closedWonLeads(),
                model.crm().closedLostLeads(),
                model.crm().conversionRate()
        );

        DealerTestDriveMetricsResource testDrives = model.testDrives() == null ? null : new DealerTestDriveMetricsResource(
                model.testDrives().totalTestDrives(),
                model.testDrives().pendingTestDrives(),
                model.testDrives().confirmedTestDrives(),
                model.testDrives().completedTestDrives(),
                model.testDrives().cancelledTestDrives()
        );

        DealerFinancingMetricsResource financing = model.financing() == null ? null : new DealerFinancingMetricsResource(
                model.financing().totalApplicationsReceived(),
                model.financing().pendingApplications(),
                model.financing().approvedApplications(),
                model.financing().rejectedApplications()
        );

        return new DealerDashboardResource(
                model.dealerUserId(),
                inventory,
                crm,
                testDrives,
                financing
        );
    }
}

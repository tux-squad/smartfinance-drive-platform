package com.smartfinance.smartfinancedriveplatform.analytics.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetAdminDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetFinancialInstitutionDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.*;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.repositories.SubscriptionJpaRepository;
import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.repositories.SpringDataVehicleRepository;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.ProspectRepository;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.TestDriveRepository;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities.CreditApplicationPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.repositories.SpringDataCreditApplicationRepository;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.repositories.SpringDataSimulationRepository;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories.SpringDataUserRepository;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.entities.RateBenchmark;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.RateBenchmarkId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.repositories.SpringDataDealershipRepository;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.repositories.SpringDataFinancialEntityRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsQueryServiceImpl Unit Tests")
class AnalyticsQueryServiceImplTest {

    @Mock
    private VehicleQueryService vehicleQueryService;

    @Mock
    private ProspectRepository prospectRepository;

    @Mock
    private TestDriveRepository testDriveRepository;

    @Mock
    private DealershipRepository dealershipRepository;

    @Mock
    private FinancialEntityQueryService financialEntityQueryService;

    @Mock
    private SpringDataCreditApplicationRepository creditApplicationRepository;

    @Mock
    private SpringDataSimulationRepository simulationRepository;

    @Mock
    private SpringDataDealershipRepository springDataDealershipRepository;

    @Mock
    private SpringDataFinancialEntityRepository springDataFinancialEntityRepository;

    @Mock
    private SpringDataUserRepository springDataUserRepository;

    @Mock
    private SpringDataVehicleRepository springDataVehicleRepository;

    @Mock
    private SubscriptionJpaRepository subscriptionJpaRepository;

    @InjectMocks
    private AnalyticsQueryServiceImpl analyticsQueryService;

    @Test
    @DisplayName("Should aggregate dealer dashboard metrics across inventory, CRM, test drives, and financing")
    void shouldAggregateDealerDashboardMetrics() {
        String dealerUserId = "dealer-user-1";
        UUID v1Id = UUID.randomUUID();
        UUID v2Id = UUID.randomUUID();

        Vehicle v1 = new Vehicle(
                new VehicleId(v1Id),
                new UserId(dealerUserId),
                new FinancialEntityId(UUID.randomUUID()),
                "Toyota", "Corolla", 2023, "NEW",
                new Money(BigDecimal.valueOf(80000.00), "PEN"),
                "img1.jpg", "ACTIVE", 0, "AUTOMATIC", "2.0L", "FWD", List.of()
        );

        Vehicle v2 = new Vehicle(
                new VehicleId(v2Id),
                new UserId(dealerUserId),
                new FinancialEntityId(UUID.randomUUID()),
                "Honda", "Civic", 2022, "USED",
                new Money(BigDecimal.valueOf(25000.00), "USD"),
                "img2.jpg", "SOLD", 15000, "MANUAL", "1.5L", "FWD", List.of()
        );

        when(vehicleQueryService.handle(any(GetVehiclesByUserIdQuery.class)))
                .thenReturn(List.of(v1, v2));

        Prospect p1 = new Prospect(dealerUserId, "buyer-1", "Carlos Mendoza", "carlos@test.com", "987654321", v1Id, "agent-1");
        p1.setStatus("NEW");

        Prospect p2 = new Prospect(dealerUserId, "buyer-2", "Maria Lopez", "maria@test.com", "912345678", v2Id, "agent-1");
        p2.setStatus("CLOSED_WON");

        when(prospectRepository.findAllByDealerUserId(dealerUserId)).thenReturn(List.of(p1, p2));

        Dealership dealership = new Dealership(
                new DealershipId(UUID.randomUUID()),
                dealerUserId,
                "20123456789",
                "Auto Motors SAC",
                "Av. Javier Prado 123",
                "987654321",
                "contacto@automotors.pe",
                "https://auto.pe",
                "Description",
                "8am-6pm",
                5.0,
                null,
                null,
                true
        );
        when(dealershipRepository.findByUserId(dealerUserId)).thenReturn(Optional.of(dealership));

        TestDrive td1 = new TestDrive(
                new TestDriveId(UUID.randomUUID()),
                "buyer-1",
                v1Id,
                dealership.getId().value(),
                LocalDateTime.now(),
                "COMPLETED",
                "Great test drive"
        );
        when(testDriveRepository.findAllByDealershipId(dealership.getId().value())).thenReturn(List.of(td1));

        CreditApplicationPersistenceEntity app1 = new CreditApplicationPersistenceEntity();
        app1.setStatus("APPROVED");
        app1.setVehicleId(v1Id);

        CreditApplicationPersistenceEntity app2 = new CreditApplicationPersistenceEntity();
        app2.setStatus("PENDING");
        app2.setVehicleId(v2Id);

        when(creditApplicationRepository.findAllByVehicleIdIn(List.of(v1Id, v2Id)))
                .thenReturn(List.of(app1, app2));

        DealerDashboardMetrics result = analyticsQueryService.handle(new GetDealerDashboardMetricsQuery(dealerUserId));

        assertNotNull(result);
        assertEquals(dealerUserId, result.dealerUserId());

        // Inventory checks
        assertEquals(2, result.inventory().totalVehicles());
        assertEquals(1, result.inventory().availableVehicles());
        assertEquals(1, result.inventory().soldVehicles());
        assertEquals(0, result.inventory().reservedVehicles());
        assertEquals(0, BigDecimal.valueOf(80000.00).compareTo(result.inventory().totalInventoryValuePen()));
        assertEquals(0, BigDecimal.valueOf(25000.00).compareTo(result.inventory().totalInventoryValueUsd()));

        // CRM checks
        assertEquals(2, result.crm().totalLeads());
        assertEquals(1, result.crm().newLeads());
        assertEquals(1, result.crm().closedWonLeads());
        assertEquals(50.0, result.crm().conversionRate());

        // Test drives checks
        assertEquals(1, result.testDrives().totalTestDrives());
        assertEquals(1, result.testDrives().completedTestDrives());

        // Financing checks
        assertEquals(2, result.financing().totalApplicationsReceived());
        assertEquals(1, result.financing().approvedApplications());
        assertEquals(1, result.financing().pendingApplications());
    }

    @Test
    @DisplayName("Should aggregate financial institution dashboard metrics")
    void shouldAggregateFinancialInstitutionDashboardMetrics() {
        UUID bankId = UUID.randomUUID();

        FinancialEntity bank = new FinancialEntity(
                new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(bankId),
                "Banco Internacional",
                null,
                null,
                new ArrayList<>()
        );

        RateBenchmark bench1 = new RateBenchmark(
                new RateBenchmarkId(UUID.randomUUID()),
                "TEA",
                new Percent(BigDecimal.valueOf(14.0)),
                "PEN",
                "SBS",
                "https://sbs.gob.pe",
                LocalDate.now()
        );
        RateBenchmark bench2 = new RateBenchmark(
                new RateBenchmarkId(UUID.randomUUID()),
                "TEA",
                new Percent(BigDecimal.valueOf(16.0)),
                "PEN",
                "SBS",
                "https://sbs.gob.pe",
                LocalDate.now()
        );
        bank.addRateBenchmark(bench1);
        bank.addRateBenchmark(bench2);

        when(financialEntityQueryService.handle(new GetFinancialEntityByIdQuery(new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(bankId))))
                .thenReturn(Optional.of(bank));

        CreditApplicationPersistenceEntity app1 = new CreditApplicationPersistenceEntity();
        app1.setStatus("APPROVED");
        app1.setRequestedAmount(BigDecimal.valueOf(60000.00));

        CreditApplicationPersistenceEntity app2 = new CreditApplicationPersistenceEntity();
        app2.setStatus("DISBURSED");
        app2.setRequestedAmount(BigDecimal.valueOf(40000.00));

        CreditApplicationPersistenceEntity app3 = new CreditApplicationPersistenceEntity();
        app3.setStatus("REJECTED");
        app3.setRequestedAmount(BigDecimal.valueOf(30000.00));

        when(creditApplicationRepository.findAllByFinancialEntityId(bankId))
                .thenReturn(List.of(app1, app2, app3));

        Optional<FinancialInstitutionDashboardMetrics> opt =
                analyticsQueryService.handle(new GetFinancialInstitutionDashboardMetricsQuery(bankId));

        assertTrue(opt.isPresent());
        FinancialInstitutionDashboardMetrics metrics = opt.get();
        assertEquals(bankId, metrics.financialEntityId());
        assertEquals("Banco Internacional", metrics.financialEntityName());
        assertEquals(3, metrics.totalApplicationsReceived());
        assertEquals(1, metrics.approvedApplications());
        assertEquals(1, metrics.disbursedApplications());
        assertEquals(1, metrics.rejectedApplications());
        assertEquals(66.7, metrics.approvalRate());
        assertEquals(0, BigDecimal.valueOf(130000.00).compareTo(metrics.totalRequestedVolumePen()));
        assertEquals(0, BigDecimal.valueOf(100000.00).compareTo(metrics.totalDisbursedVolumePen()));
        assertEquals(0, BigDecimal.valueOf(15.00).compareTo(metrics.averageTea()));
        assertEquals(2, metrics.activeRateBenchmarksCount());
    }

    @Test
    @DisplayName("Should return empty when financial institution is not found")
    void shouldReturnEmptyWhenFinancialInstitutionNotFound() {
        UUID bankId = UUID.randomUUID();
        when(financialEntityQueryService.handle(new GetFinancialEntityByIdQuery(new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(bankId))))
                .thenReturn(Optional.empty());

        Optional<FinancialInstitutionDashboardMetrics> opt =
                analyticsQueryService.handle(new GetFinancialInstitutionDashboardMetricsQuery(bankId));

        assertTrue(opt.isEmpty());
    }

    @Test
    @DisplayName("Should aggregate admin platform-wide metrics including subscriptions MRR")
    void shouldAggregateAdminDashboardMetrics() {
        when(springDataDealershipRepository.count()).thenReturn(10L);
        when(springDataDealershipRepository.countByActive(true)).thenReturn(8);
        when(springDataFinancialEntityRepository.count()).thenReturn(4L);
        when(springDataUserRepository.count()).thenReturn(150L);
        when(springDataVehicleRepository.count()).thenReturn(75L);
        when(creditApplicationRepository.count()).thenReturn(2L);
        when(simulationRepository.count()).thenReturn(1L);

        Plan plan = new Plan("Premium Tier", "Full access", BigDecimal.valueOf(150.00), "USD",
                BillingCycle.MONTHLY, 50, 100);

        Subscription sub1 = new Subscription("dealer-1", plan, true);
        Subscription sub2 = new Subscription("dealer-2", plan, true);

        when(subscriptionJpaRepository.findAll()).thenReturn(List.of(sub1, sub2));

        AdminDashboardMetrics metrics = analyticsQueryService.handle(new GetAdminDashboardMetricsQuery());

        assertNotNull(metrics);
        assertEquals(10, metrics.totalDealerships());
        assertEquals(8, metrics.activeDealerships());
        assertEquals(4, metrics.totalFinancialEntities());
        assertEquals(150, metrics.totalRegisteredUsers());
        assertEquals(75, metrics.totalVehiclesListed());
        assertEquals(2, metrics.totalCreditApplications());
        assertEquals(1, metrics.totalSimulationsRun());
        assertEquals(2, metrics.totalActiveSubscriptions());
        assertEquals(0, BigDecimal.valueOf(300.00).compareTo(metrics.estimatedMonthlyRecurringRevenueUsd()));
    }
}

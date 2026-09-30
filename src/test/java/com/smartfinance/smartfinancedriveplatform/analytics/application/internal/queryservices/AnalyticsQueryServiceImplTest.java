package com.smartfinance.smartfinancedriveplatform.analytics.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetAdminDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetFinancialInstitutionDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.*;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.SubscriptionRepository;
import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.repositories.VehicleRepository;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.valueobjects.TestDriveId;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.ProspectRepository;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.TestDriveRepository;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.CreditApplicationId;
import com.smartfinance.smartfinancedriveplatform.financing.domain.repositories.CreditApplicationRepository;
import com.smartfinance.smartfinancedriveplatform.financing.domain.repositories.SimulationRepository;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.UserRepository;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.entities.RateBenchmark;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.RateBenchmarkId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository;
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
    private VehicleRepository vehicleRepository;

    @Mock
    private ProspectRepository prospectRepository;

    @Mock
    private TestDriveRepository testDriveRepository;

    @Mock
    private DealershipRepository dealershipRepository;

    @Mock
    private FinancialEntityQueryService financialEntityQueryService;

    @Mock
    private FinancialEntityRepository financialEntityRepository;

    @Mock
    private CreditApplicationRepository creditApplicationRepository;

    @Mock
    private SimulationRepository simulationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

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

        CreditApplication app1 = new CreditApplication(
                new CreditApplicationId(UUID.randomUUID()),
                "user-1",
                v1Id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Money(BigDecimal.valueOf(50000.00), "PEN"),
                new Money(BigDecimal.valueOf(10000.00), "PEN"),
                36,
                new Money(BigDecimal.valueOf(5000.00), "PEN"),
                "EMPLOYED",
                "APPROVED",
                null
        );

        CreditApplication app2 = new CreditApplication(
                new CreditApplicationId(UUID.randomUUID()),
                "user-2",
                v2Id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Money(BigDecimal.valueOf(20000.00), "USD"),
                new Money(BigDecimal.valueOf(4000.00), "USD"),
                24,
                new Money(BigDecimal.valueOf(3000.00), "USD"),
                "EMPLOYED",
                "PENDING",
                null
        );

        when(creditApplicationRepository.findAllByVehicleIdIn(List.of(v1Id, v2Id)))
                .thenReturn(List.of(app1, app2));

        DealerDashboardMetrics result = analyticsQueryService.handle(new GetDealerDashboardMetricsQuery(dealerUserId, MetricPeriod.ALL_TIME));

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

        CreditApplication app1 = new CreditApplication(
                new CreditApplicationId(UUID.randomUUID()),
                "user-1",
                UUID.randomUUID(),
                bankId,
                UUID.randomUUID(),
                new Money(BigDecimal.valueOf(60000.00), "PEN"),
                new Money(BigDecimal.valueOf(10000.00), "PEN"),
                36,
                new Money(BigDecimal.valueOf(5000.00), "PEN"),
                "EMPLOYED",
                "APPROVED",
                null
        );

        CreditApplication app2 = new CreditApplication(
                new CreditApplicationId(UUID.randomUUID()),
                "user-2",
                UUID.randomUUID(),
                bankId,
                UUID.randomUUID(),
                new Money(BigDecimal.valueOf(40000.00), "PEN"),
                new Money(BigDecimal.valueOf(8000.00), "PEN"),
                24,
                new Money(BigDecimal.valueOf(4000.00), "PEN"),
                "EMPLOYED",
                "DISBURSED",
                null
        );

        CreditApplication app3 = new CreditApplication(
                new CreditApplicationId(UUID.randomUUID()),
                "user-3",
                UUID.randomUUID(),
                bankId,
                UUID.randomUUID(),
                new Money(BigDecimal.valueOf(30000.00), "PEN"),
                new Money(BigDecimal.valueOf(6000.00), "PEN"),
                48,
                new Money(BigDecimal.valueOf(3000.00), "PEN"),
                "EMPLOYED",
                "REJECTED",
                null
        );

        when(creditApplicationRepository.findAllByFinancialEntityId(bankId))
                .thenReturn(List.of(app1, app2, app3));
        when(simulationRepository.countByFinancialEntityId(bankId.toString()))
                .thenReturn(12);

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
        assertEquals(12, metrics.totalSimulationsCount());
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
    @DisplayName("Should aggregate admin platform-wide metrics including subscriptions MRR normalized")
    void shouldAggregateAdminDashboardMetrics() {
        when(dealershipRepository.count()).thenReturn(10L);
        when(dealershipRepository.countByActive(true)).thenReturn(8);
        when(financialEntityRepository.count()).thenReturn(4L);
        when(userRepository.count()).thenReturn(150L);
        when(vehicleRepository.count()).thenReturn(75L);
        when(creditApplicationRepository.count()).thenReturn(2L);
        when(simulationRepository.count()).thenReturn(1L);

        Plan planUsdMonthly = new Plan("Premium Tier", "Full access", BigDecimal.valueOf(150.00), "USD",
                BillingCycle.MONTHLY, 50, 100);

        Plan planAnnualPen = new Plan("Enterprise Tier", "Full access annual", BigDecimal.valueOf(4500.00), "PEN",
                BillingCycle.ANNUAL, 100, 500);

        Subscription sub1 = new Subscription("dealer-1", planUsdMonthly, true);
        Subscription sub2 = new Subscription("dealer-2", planAnnualPen, true);

        when(subscriptionRepository.findAllByStatus(SubscriptionStatus.ACTIVE)).thenReturn(List.of(sub1, sub2));

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

        // sub1: 150.00 USD
        // sub2: 4500 PEN / 12 = 375 PEN. 375 PEN / 3.75 = 100.00 USD.
        // Total MRR: 150.00 + 100.00 = 250.00 USD
        assertEquals(0, BigDecimal.valueOf(250.00).compareTo(metrics.estimatedMonthlyRecurringRevenueUsd()));
    }
}

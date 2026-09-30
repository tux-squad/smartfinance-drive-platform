package com.smartfinance.smartfinancedriveplatform.analytics.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.analytics.application.queryservices.AnalyticsQueryService;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetAdminDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetFinancialInstitutionDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.*;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.smartfinance.smartfinancedriveplatform.billing.infrastructure.persistence.jpa.repositories.SubscriptionJpaRepository;
import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.persistence.jpa.repositories.SpringDataVehicleRepository;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.ProspectRepository;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.TestDriveRepository;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.entities.CreditApplicationPersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.repositories.SpringDataCreditApplicationRepository;
import com.smartfinance.smartfinancedriveplatform.financing.infrastructure.persistence.jpa.repositories.SpringDataSimulationRepository;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.persistence.jpa.repositories.SpringDataUserRepository;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.repositories.SpringDataDealershipRepository;
import com.smartfinance.smartfinancedriveplatform.partners.infrastructure.persistence.jpa.repositories.SpringDataFinancialEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    private final VehicleQueryService vehicleQueryService;
    private final ProspectRepository prospectRepository;
    private final TestDriveRepository testDriveRepository;
    private final DealershipRepository dealershipRepository;
    private final FinancialEntityQueryService financialEntityQueryService;
    private final SpringDataCreditApplicationRepository creditApplicationRepository;
    private final SpringDataSimulationRepository simulationRepository;
    private final SpringDataDealershipRepository springDataDealershipRepository;
    private final SpringDataFinancialEntityRepository springDataFinancialEntityRepository;
    private final SpringDataUserRepository springDataUserRepository;
    private final SpringDataVehicleRepository springDataVehicleRepository;
    private final SubscriptionJpaRepository subscriptionJpaRepository;

    public AnalyticsQueryServiceImpl(VehicleQueryService vehicleQueryService,
                                     ProspectRepository prospectRepository,
                                     TestDriveRepository testDriveRepository,
                                     DealershipRepository dealershipRepository,
                                     FinancialEntityQueryService financialEntityQueryService,
                                     SpringDataCreditApplicationRepository creditApplicationRepository,
                                     SpringDataSimulationRepository simulationRepository,
                                     SpringDataDealershipRepository springDataDealershipRepository,
                                     SpringDataFinancialEntityRepository springDataFinancialEntityRepository,
                                     SpringDataUserRepository springDataUserRepository,
                                     SpringDataVehicleRepository springDataVehicleRepository,
                                     SubscriptionJpaRepository subscriptionJpaRepository) {
        this.vehicleQueryService = vehicleQueryService;
        this.prospectRepository = prospectRepository;
        this.testDriveRepository = testDriveRepository;
        this.dealershipRepository = dealershipRepository;
        this.financialEntityQueryService = financialEntityQueryService;
        this.creditApplicationRepository = creditApplicationRepository;
        this.simulationRepository = simulationRepository;
        this.springDataDealershipRepository = springDataDealershipRepository;
        this.springDataFinancialEntityRepository = springDataFinancialEntityRepository;
        this.springDataUserRepository = springDataUserRepository;
        this.springDataVehicleRepository = springDataVehicleRepository;
        this.subscriptionJpaRepository = subscriptionJpaRepository;
    }

    @Override
    public DealerDashboardMetrics handle(GetDealerDashboardMetricsQuery query) {
        String dealerUserId = query.dealerUserId();

        // 1. Inventory metrics
        List<Vehicle> vehicles = vehicleQueryService.handle(new GetVehiclesByUserIdQuery(new UserId(dealerUserId)));
        int totalVehicles = vehicles.size();
        int availableVehicles = (int) vehicles.stream()
                .filter(v -> v.getStatus() != null && ("AVAILABLE".equalsIgnoreCase(v.getStatus()) || "ACTIVE".equalsIgnoreCase(v.getStatus())))
                .count();
        int reservedVehicles = (int) vehicles.stream()
                .filter(v -> v.getStatus() != null && "RESERVED".equalsIgnoreCase(v.getStatus()))
                .count();
        int soldVehicles = (int) vehicles.stream()
                .filter(v -> v.getStatus() != null && "SOLD".equalsIgnoreCase(v.getStatus()))
                .count();

        BigDecimal totalPen = vehicles.stream()
                .filter(v -> v.getPrice() != null && "PEN".equalsIgnoreCase(v.getPrice().currency()))
                .map(v -> v.getPrice().amount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUsd = vehicles.stream()
                .filter(v -> v.getPrice() != null && "USD".equalsIgnoreCase(v.getPrice().currency()))
                .map(v -> v.getPrice().amount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DealerInventoryMetrics inventory = new DealerInventoryMetrics(
                totalVehicles, availableVehicles, reservedVehicles, soldVehicles, totalPen, totalUsd
        );

        // 2. CRM Leads metrics (filtered by requested period)
        MetricPeriod period = query.period() != null ? query.period() : MetricPeriod.ALL_TIME;
        java.time.Instant fromInstant = null;
        java.time.LocalDateTime fromDateTime = null;
        if (period == MetricPeriod.LAST_30_DAYS) {
            fromInstant = java.time.Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
            fromDateTime = java.time.LocalDateTime.now().minusDays(30);
        } else if (period == MetricPeriod.LAST_7_DAYS) {
            fromInstant = java.time.Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS);
            fromDateTime = java.time.LocalDateTime.now().minusDays(7);
        }

        java.time.Instant finalFromInstant = fromInstant;
        java.time.LocalDateTime finalFromDateTime = fromDateTime;

        List<Prospect> rawProspects = prospectRepository.findAllByDealerUserId(dealerUserId);
        List<Prospect> prospects = (finalFromInstant == null)
                ? rawProspects
                : rawProspects.stream()
                        .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(finalFromInstant))
                        .toList();

        int totalLeads = prospects.size();
        int newLeads = (int) prospects.stream().filter(p -> "NEW".equalsIgnoreCase(p.getStatus())).count();
        int contactedLeads = (int) prospects.stream().filter(p -> "CONTACTED".equalsIgnoreCase(p.getStatus())).count();
        int qualifiedLeads = (int) prospects.stream().filter(p -> "QUALIFIED".equalsIgnoreCase(p.getStatus())).count();
        int inNegotiationLeads = (int) prospects.stream().filter(p -> "IN_NEGOTIATION".equalsIgnoreCase(p.getStatus())).count();
        int closedWonLeads = (int) prospects.stream().filter(p -> "CLOSED_WON".equalsIgnoreCase(p.getStatus())).count();
        int closedLostLeads = (int) prospects.stream().filter(p -> "CLOSED_LOST".equalsIgnoreCase(p.getStatus())).count();

        double conversionRate = totalLeads > 0
                ? Math.round((closedWonLeads * 100.0 / totalLeads) * 10.0) / 10.0
                : 0.0;

        DealerCrmMetrics crm = new DealerCrmMetrics(
                totalLeads, newLeads, contactedLeads, qualifiedLeads, inNegotiationLeads, closedWonLeads, closedLostLeads, conversionRate
        );

        // 3. Test Drive metrics (filtered by requested period)
        var dealershipOpt = dealershipRepository.findByUserId(dealerUserId);
        List<TestDrive> rawTestDrives = dealershipOpt.isPresent()
                ? testDriveRepository.findAllByDealershipId(dealershipOpt.get().getId().value())
                : Collections.emptyList();

        List<TestDrive> testDrives = (finalFromDateTime == null)
                ? rawTestDrives
                : rawTestDrives.stream()
                        .filter(t -> t.getScheduledDateTime() != null && !t.getScheduledDateTime().isBefore(finalFromDateTime))
                        .toList();

        int totalTestDrives = testDrives.size();
        int pendingTestDrives = (int) testDrives.stream().filter(t -> "PENDING".equalsIgnoreCase(t.getStatus())).count();
        int confirmedTestDrives = (int) testDrives.stream().filter(t -> "CONFIRMED".equalsIgnoreCase(t.getStatus())).count();
        int completedTestDrives = (int) testDrives.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        int cancelledTestDrives = (int) testDrives.stream().filter(t -> "CANCELLED".equalsIgnoreCase(t.getStatus())).count();

        DealerTestDriveMetrics testDriveMetrics = new DealerTestDriveMetrics(
                totalTestDrives, pendingTestDrives, confirmedTestDrives, completedTestDrives, cancelledTestDrives
        );

        // 4. Financing applications for dealer's vehicles
        List<UUID> vehicleIds = vehicles.stream().map(v -> v.getId().value()).toList();
        List<CreditApplicationPersistenceEntity> applications = vehicleIds.isEmpty()
                ? Collections.emptyList()
                : creditApplicationRepository.findAllByVehicleIdIn(vehicleIds);

        int totalApplications = applications.size();
        int pendingApplications = (int) applications.stream().filter(a -> "PENDING".equalsIgnoreCase(a.getStatus()) || "SUBMITTED".equalsIgnoreCase(a.getStatus())).count();
        int approvedApplications = (int) applications.stream().filter(a -> "APPROVED".equalsIgnoreCase(a.getStatus())).count();
        int rejectedApplications = (int) applications.stream().filter(a -> "REJECTED".equalsIgnoreCase(a.getStatus())).count();

        DealerFinancingMetrics financing = new DealerFinancingMetrics(
                totalApplications, pendingApplications, approvedApplications, rejectedApplications
        );

        return new DealerDashboardMetrics(
                dealerUserId,
                inventory,
                crm,
                testDriveMetrics,
                financing,
                period.name()
        );
    }

    @Override
    public Optional<FinancialInstitutionDashboardMetrics> handle(GetFinancialInstitutionDashboardMetricsQuery query) {
        UUID financialEntityId = query.financialEntityId();
        Optional<FinancialEntity> entityOpt = financialEntityQueryService.handle(
                new GetFinancialEntityByIdQuery(new FinancialEntityId(financialEntityId))
        );

        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        FinancialEntity entity = entityOpt.get();
        int activeRateBenchmarks = entity.getRateBenchmarks().size();

        // Applications for this bank
        List<CreditApplicationPersistenceEntity> applications = creditApplicationRepository.findAllByFinancialEntityId(financialEntityId);
        int totalApplications = applications.size();
        int underReview = (int) applications.stream()
                .filter(a -> "PENDING".equalsIgnoreCase(a.getStatus()) || "UNDER_REVIEW".equalsIgnoreCase(a.getStatus()) || "SUBMITTED".equalsIgnoreCase(a.getStatus())).count();
        int approved = (int) applications.stream()
                .filter(a -> "APPROVED".equalsIgnoreCase(a.getStatus())).count();
        int rejected = (int) applications.stream()
                .filter(a -> "REJECTED".equalsIgnoreCase(a.getStatus())).count();
        int disbursed = (int) applications.stream()
                .filter(a -> "DISBURSED".equalsIgnoreCase(a.getStatus())).count();

        double approvalRate = totalApplications > 0
                ? Math.round(((approved + disbursed) * 100.0 / totalApplications) * 10.0) / 10.0
                : 0.0;

        BigDecimal totalRequestedPen = applications.stream()
                .map(CreditApplicationPersistenceEntity::getRequestedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDisbursedPen = applications.stream()
                .filter(a -> "APPROVED".equalsIgnoreCase(a.getStatus()) || "DISBURSED".equalsIgnoreCase(a.getStatus()))
                .map(CreditApplicationPersistenceEntity::getRequestedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average TEA from bank benchmarks
        BigDecimal averageTea = entity.getRateBenchmarks().isEmpty()
                ? BigDecimal.valueOf(14.5)
                : BigDecimal.valueOf(entity.getRateBenchmarks().stream()
                        .filter(b -> b.getAnnualRate() != null && b.getAnnualRate().value() != null)
                        .mapToDouble(b -> b.getAnnualRate().value().doubleValue())
                        .average().orElse(14.5))
                        .setScale(2, RoundingMode.HALF_UP);

        int simulationsCount = simulationRepository.countByFinancialEntityId(financialEntityId.toString());

        return Optional.of(new FinancialInstitutionDashboardMetrics(
                financialEntityId,
                entity.getName(),
                totalApplications,
                underReview,
                approved,
                rejected,
                disbursed,
                approvalRate,
                totalRequestedPen,
                totalDisbursedPen,
                averageTea,
                activeRateBenchmarks,
                simulationsCount
        ));
    }

    @Override
    public AdminDashboardMetrics handle(GetAdminDashboardMetricsQuery query) {
        int totalDealerships = (int) springDataDealershipRepository.count();
        int activeDealerships = springDataDealershipRepository.countByActive(true);
        int totalFinancialEntities = (int) springDataFinancialEntityRepository.count();
        int totalUsers = (int) springDataUserRepository.count();
        int totalVehicles = (int) springDataVehicleRepository.count();
        int totalCreditApplications = (int) creditApplicationRepository.count();
        int totalSimulations = (int) simulationRepository.count();

        List<Subscription> activeSubscriptions = subscriptionJpaRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();
        int totalActiveSubscriptions = activeSubscriptions.size();

        BigDecimal mrr = activeSubscriptions.stream()
                .filter(s -> s.getPlan() != null && s.getPlan().getPrice() != null)
                .map(s -> s.getPlan().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AdminDashboardMetrics(
                totalDealerships,
                activeDealerships,
                totalFinancialEntities,
                totalUsers,
                totalVehicles,
                totalCreditApplications,
                totalSimulations,
                totalActiveSubscriptions,
                mrr
        );
    }
}

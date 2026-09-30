package com.smartfinance.smartfinancedriveplatform.analytics.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.analytics.application.queryservices.AnalyticsQueryService;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetAdminDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetDealerDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.queries.GetFinancialInstitutionDashboardMetricsQuery;
import com.smartfinance.smartfinancedriveplatform.analytics.domain.model.valueobjects.*;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.SubscriptionRepository;
import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates.Vehicle;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehiclesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.repositories.VehicleRepository;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.ProspectRepository;
import com.smartfinance.smartfinancedriveplatform.crm.domain.repositories.TestDriveRepository;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates.CreditApplication;
import com.smartfinance.smartfinancedriveplatform.financing.domain.repositories.CreditApplicationRepository;
import com.smartfinance.smartfinancedriveplatform.financing.domain.repositories.SimulationRepository;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.UserRepository;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsQueryServiceImpl.class);
    public static final BigDecimal DEFAULT_FX_PEN_TO_USD = BigDecimal.valueOf(3.75);

    @Value("${analytics.fx.pen-to-usd:3.75}")
    private BigDecimal fxPenToUsd = DEFAULT_FX_PEN_TO_USD;

    @PostConstruct
    public void validateConfiguration() {
        if (fxPenToUsd == null || fxPenToUsd.compareTo(BigDecimal.ZERO) <= 0) {
            LOGGER.warn("Invalid FX rate configured: {}. Falling back to default: {}", fxPenToUsd, DEFAULT_FX_PEN_TO_USD);
            this.fxPenToUsd = DEFAULT_FX_PEN_TO_USD;
        }
    }

    private final VehicleQueryService vehicleQueryService;
    private final VehicleRepository vehicleRepository;
    private final ProspectRepository prospectRepository;
    private final TestDriveRepository testDriveRepository;
    private final DealershipRepository dealershipRepository;
    private final FinancialEntityQueryService financialEntityQueryService;
    private final FinancialEntityRepository financialEntityRepository;
    private final CreditApplicationRepository creditApplicationRepository;
    private final SimulationRepository simulationRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public AnalyticsQueryServiceImpl(VehicleQueryService vehicleQueryService,
                                     VehicleRepository vehicleRepository,
                                     ProspectRepository prospectRepository,
                                     TestDriveRepository testDriveRepository,
                                     DealershipRepository dealershipRepository,
                                     FinancialEntityQueryService financialEntityQueryService,
                                     FinancialEntityRepository financialEntityRepository,
                                     CreditApplicationRepository creditApplicationRepository,
                                     SimulationRepository simulationRepository,
                                     UserRepository userRepository,
                                     SubscriptionRepository subscriptionRepository) {
        this.vehicleQueryService = vehicleQueryService;
        this.vehicleRepository = vehicleRepository;
        this.prospectRepository = prospectRepository;
        this.testDriveRepository = testDriveRepository;
        this.dealershipRepository = dealershipRepository;
        this.financialEntityQueryService = financialEntityQueryService;
        this.financialEntityRepository = financialEntityRepository;
        this.creditApplicationRepository = creditApplicationRepository;
        this.simulationRepository = simulationRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
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
        List<CreditApplication> applications = vehicleIds.isEmpty()
                ? Collections.emptyList()
                : creditApplicationRepository.findAllByVehicleIdIn(vehicleIds);

        int totalApplications = applications.size();
        int pendingApplications = (int) applications.stream()
                .filter(a -> a.getStatus() != null && ("PENDING".equalsIgnoreCase(a.getStatus()) || "SUBMITTED".equalsIgnoreCase(a.getStatus()) || "IN_REVIEW".equalsIgnoreCase(a.getStatus())))
                .count();
        int approvedApplications = (int) applications.stream()
                .filter(a -> a.getStatus() != null && ("APPROVED".equalsIgnoreCase(a.getStatus()) || "PRE_APPROVED".equalsIgnoreCase(a.getStatus())))
                .count();
        int rejectedApplications = (int) applications.stream()
                .filter(a -> a.getStatus() != null && "REJECTED".equalsIgnoreCase(a.getStatus()))
                .count();

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
        List<CreditApplication> applications = creditApplicationRepository.findAllByFinancialEntityId(financialEntityId);
        int totalApplications = applications.size();
        int underReview = (int) applications.stream()
                .filter(a -> a.getStatus() != null && ("PENDING".equalsIgnoreCase(a.getStatus()) || "UNDER_REVIEW".equalsIgnoreCase(a.getStatus()) || "SUBMITTED".equalsIgnoreCase(a.getStatus()) || "IN_REVIEW".equalsIgnoreCase(a.getStatus())))
                .count();
        int approved = (int) applications.stream()
                .filter(a -> a.getStatus() != null && ("APPROVED".equalsIgnoreCase(a.getStatus()) || "PRE_APPROVED".equalsIgnoreCase(a.getStatus())))
                .count();
        int rejected = (int) applications.stream()
                .filter(a -> a.getStatus() != null && "REJECTED".equalsIgnoreCase(a.getStatus()))
                .count();
        int disbursed = (int) applications.stream()
                .filter(a -> a.getStatus() != null && "DISBURSED".equalsIgnoreCase(a.getStatus()))
                .count();

        double approvalRate = totalApplications > 0
                ? Math.round(((approved + disbursed) * 100.0 / totalApplications) * 10.0) / 10.0
                : 0.0;

        BigDecimal totalRequestedPen = applications.stream()
                .map(CreditApplication::getRequestedAmount)
                .filter(Objects::nonNull)
                .map(Money::amount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDisbursedPen = applications.stream()
                .filter(a -> a.getStatus() != null && ("APPROVED".equalsIgnoreCase(a.getStatus()) || "PRE_APPROVED".equalsIgnoreCase(a.getStatus()) || "DISBURSED".equalsIgnoreCase(a.getStatus()) || "ACCEPTED".equalsIgnoreCase(a.getStatus())))
                .map(CreditApplication::getRequestedAmount)
                .filter(Objects::nonNull)
                .map(Money::amount)
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
        int totalDealerships = (int) dealershipRepository.count();
        int activeDealerships = dealershipRepository.countByActive(true);
        int totalFinancialEntities = (int) financialEntityRepository.count();
        int totalUsers = (int) userRepository.count();
        int totalVehicles = (int) vehicleRepository.count();
        int totalCreditApplications = (int) creditApplicationRepository.count();
        int totalSimulations = (int) simulationRepository.count();

        List<Subscription> activeSubscriptions = subscriptionRepository.findAllByStatus(SubscriptionStatus.ACTIVE);
        int totalActiveSubscriptions = activeSubscriptions.size();

        BigDecimal mrr = activeSubscriptions.stream()
                .filter(s -> s.getPlan() != null && s.getPlan().getPrice() != null)
                .map(s -> {
                    BigDecimal price = s.getPlan().getPrice();
                    if (s.getPlan().getBillingCycle() == BillingCycle.ANNUAL) {
                        price = price.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
                    }
                    if ("PEN".equalsIgnoreCase(s.getPlan().getCurrency())) {
                        price = price.divide(getEffectiveFxRate(), 2, RoundingMode.HALF_UP);
                    }
                    return price;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

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

    public BigDecimal getFxPenToUsd() {
        return fxPenToUsd;
    }

    public BigDecimal getEffectiveFxRate() {
        if (fxPenToUsd == null || fxPenToUsd.compareTo(BigDecimal.ZERO) <= 0) {
            return DEFAULT_FX_PEN_TO_USD;
        }
        return fxPenToUsd;
    }

    public void setFxPenToUsd(BigDecimal fxPenToUsd) {
        if (fxPenToUsd == null || fxPenToUsd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("analytics.error.invalidFxRate");
        }
        this.fxPenToUsd = fxPenToUsd;
    }
}


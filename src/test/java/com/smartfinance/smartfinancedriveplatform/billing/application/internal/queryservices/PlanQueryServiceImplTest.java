package com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetAllActivePlansQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetPlanByIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanQueryServiceImpl Unit Tests")
class PlanQueryServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanQueryServiceImpl planQueryService;

    private Plan testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new Plan("DEALER_PRO", "Dealer Pro Tier", new BigDecimal("99.99"), "USD", BillingCycle.MONTHLY, 20, 100);
        ReflectionTestUtils.setField(testPlan, "id", 1L);
    }

    @Test
    @DisplayName("Should return all active plans on GetAllActivePlansQuery")
    void shouldReturnAllActivePlans() {
        when(planRepository.findAllActive()).thenReturn(List.of(testPlan));

        List<Plan> result = planQueryService.handle(new GetAllActivePlansQuery());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DEALER_PRO", result.get(0).getName());
    }

    @Test
    @DisplayName("Should return plan by ID when found on GetPlanByIdQuery")
    void shouldReturnPlanById() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        Optional<Plan> result = planQueryService.handle(new GetPlanByIdQuery(1L));

        assertTrue(result.isPresent());
        assertEquals("DEALER_PRO", result.get().getName());
    }
}

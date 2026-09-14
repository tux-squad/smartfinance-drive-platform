package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices.PlanCommandService;
import com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices.PlanQueryService;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.CreatePlanCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetAllActivePlansQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetPlanByIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.CreatePlanResource;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.PlanResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlansController Unit Tests")
class PlansControllerTest {

    @Mock
    private PlanCommandService planCommandService;

    @Mock
    private PlanQueryService planQueryService;

    @InjectMocks
    private PlansController plansController;

    private Plan testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new Plan("DEALER_PRO", "Dealer Pro Tier", new BigDecimal("99.99"), "USD", BillingCycle.MONTHLY, 20, 100);
        ReflectionTestUtils.setField(testPlan, "id", 1L);
    }

    @Test
    @DisplayName("Should return all active plans on getAllActivePlans")
    void shouldReturnAllActivePlans() {
        when(planQueryService.handle(any(GetAllActivePlansQuery.class))).thenReturn(List.of(testPlan));

        ResponseEntity<List<PlanResource>> response = plansController.getAllActivePlans();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("DEALER_PRO", response.getBody().get(0).name());
    }

    @Test
    @DisplayName("Should return plan by ID when found")
    void shouldReturnPlanByIdWhenFound() {
        when(planQueryService.handle(any(GetPlanByIdQuery.class))).thenReturn(Optional.of(testPlan));

        ResponseEntity<PlanResource> response = plansController.getPlanById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DEALER_PRO", response.getBody().name());
    }

    @Test
    @DisplayName("Should return 404 Not Found when plan by ID does not exist")
    void shouldReturnNotFoundWhenPlanDoesNotExist() {
        when(planQueryService.handle(any(GetPlanByIdQuery.class))).thenReturn(Optional.empty());

        ResponseEntity<PlanResource> response = plansController.getPlanById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 201 Created on valid createPlan resource")
    void shouldCreatePlanSuccessfully() {
        when(planCommandService.handle(any(CreatePlanCommand.class))).thenReturn(Optional.of(testPlan));

        CreatePlanResource resource = new CreatePlanResource(
                "DEALER_PRO", "Dealer Pro Tier", new BigDecimal("99.99"), "USD",
                BillingCycle.MONTHLY, 20, 100, "price_123"
        );

        ResponseEntity<PlanResource> response = plansController.createPlan(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DEALER_PRO", response.getBody().name());
    }
}

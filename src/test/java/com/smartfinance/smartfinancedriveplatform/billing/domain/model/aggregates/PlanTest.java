package com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Plan Aggregate Root Unit Tests")
class PlanTest {

    @Test
    @DisplayName("Should create Plan with default values correctly")
    void shouldCreatePlanWithDefaultValues() {
        Plan plan = new Plan("PREMIUM_DEALER", "Dealer Premium Plan", new BigDecimal("199.99"), "USD", BillingCycle.MONTHLY, 50, 200);

        assertEquals("PREMIUM_DEALER", plan.getName());
        assertEquals("Dealer Premium Plan", plan.getDescription());
        assertEquals(new BigDecimal("199.99"), plan.getPrice());
        assertEquals("USD", plan.getCurrency());
        assertEquals(BillingCycle.MONTHLY, plan.getBillingCycle());
        assertEquals(50, plan.getMaxVehicleListings());
        assertEquals(200, plan.getMaxSimulationsPerMonth());
        assertTrue(plan.isActive());
    }
}

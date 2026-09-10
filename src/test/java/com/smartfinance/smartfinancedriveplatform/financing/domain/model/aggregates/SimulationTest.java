package com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.GracePeriodType;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.VehicleInsuranceType;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Percent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Simulation Aggregate Root Tests")
class SimulationTest {

    @Test
    @DisplayName("Should create Simulation aggregate and automatically compute schedule and metrics")
    void shouldCreateSimulationAndComputeSchedule() {
        Simulation simulation = new Simulation(
                "Cotizacion BCP Toyota RAV4",
                "user-123",
                "vehicle-456",
                "bank-bcp",
                Money.of(25000.0, "USD"),
                Percent.of(20.0),
                Percent.of(0.0),
                Percent.of(12.5),
                Percent.of(0.05),
                Money.of(45.0, "USD"),
                VehicleInsuranceType.MENSUAL,
                24,
                GracePeriodType.NONE,
                0,
                Money.of(120.0, "USD"),
                Percent.of(10.0),
                LocalDate.of(2026, 3, 1)
        );

        assertNotNull(simulation.getId());
        assertEquals("Cotizacion BCP Toyota RAV4", simulation.getTitle());
        assertEquals(24, simulation.getPaymentPeriods().size());
        assertNotNull(simulation.getFinancedAmount());
        assertNotNull(simulation.getTcea());
        assertNotNull(simulation.getTir());
        assertNotNull(simulation.getVan());
    }

    @Test
    @DisplayName("Should throw exception when title is null or blank")
    void shouldThrowExceptionWhenTitleIsInvalid() {
        assertThrows(DomainValidationException.class, () -> new Simulation(
                "",
                "user-123",
                "vehicle-456",
                "bank-bcp",
                Money.of(25000.0, "USD"),
                Percent.of(20.0),
                Percent.of(0.0),
                Percent.of(12.5),
                Percent.of(0.05),
                Money.of(45.0, "USD"),
                VehicleInsuranceType.MENSUAL,
                24,
                GracePeriodType.NONE,
                0,
                Money.of(120.0, "USD"),
                Percent.of(10.0),
                LocalDate.of(2026, 3, 1)
        ));
    }
}

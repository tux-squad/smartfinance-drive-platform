package com.smartfinance.smartfinancedriveplatform.projections.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.MotorizationType;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DepreciationProjection Aggregate Root Tests")
class DepreciationProjectionTest {

    @Test
    @DisplayName("Should create DepreciationProjection aggregate and automatically compute values")
    void shouldCreateDepreciationProjectionAndComputeValues() {
        DepreciationProjection projection = new DepreciationProjection(
                "veh-100",
                "sim-200",
                Money.of(25000.0, "USD"),
                2025,
                MotorizationType.COMBUSTION,
                Money.of(12000.0, "USD")
        );

        assertNotNull(projection.getId());
        assertEquals("veh-100", projection.getVehicleId());
        assertEquals("sim-200", projection.getSimulationId());
        assertNotNull(projection.getProjectedValue2Years());
        assertNotNull(projection.getProjectedValue3Years());
        assertNotNull(projection.getProjectedValue5Years());
        assertNotNull(projection.getRecommendedAction());
        assertNotNull(projection.getAdvisoryNotes());
    }

    @Test
    @DisplayName("Should throw exception when vehicleId is null or blank")
    void shouldThrowExceptionWhenVehicleIdIsInvalid() {
        assertThrows(DomainValidationException.class, () -> new DepreciationProjection(
                "",
                "sim-200",
                Money.of(25000.0, "USD"),
                2025,
                MotorizationType.COMBUSTION,
                Money.of(12000.0, "USD")
        ));
    }
}

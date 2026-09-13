package com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security;

import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.queries.GetVehicleByIdQuery;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.financing.application.queryservices.SimulationQueryService;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.queries.GetSimulationByIdQuery;
import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.SimulationId;
import com.smartfinance.smartfinancedriveplatform.profiles.application.queryservices.ProfileQueryService;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.queries.GetProfileByIdQuery;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Spring Security Bean for evaluating resource ownership in @PreAuthorize annotations.
 */
@Component("ownershipChecker")
public class OwnershipChecker {

    private final VehicleQueryService vehicleQueryService;
    private final ProfileQueryService profileQueryService;
    private final SimulationQueryService simulationQueryService;

    public OwnershipChecker(VehicleQueryService vehicleQueryService,
                            ProfileQueryService profileQueryService,
                            SimulationQueryService simulationQueryService) {
        this.vehicleQueryService = vehicleQueryService;
        this.profileQueryService = profileQueryService;
        this.simulationQueryService = simulationQueryService;
    }

    /**
     * Checks if the currently authenticated user owns the specified vehicle.
     *
     * @param vehicleId      The vehicle UUID.
     * @param authentication Spring Security Authentication.
     * @return true if owner or admin, false otherwise.
     */
    public boolean isVehicleOwner(UUID vehicleId, Authentication authentication) {
        if (isAdmin(authentication)) return true;
        String currentUserId = getUserIdFromAuthentication(authentication);
        if (currentUserId == null || vehicleId == null) return false;

        return vehicleQueryService.handle(new GetVehicleByIdQuery(new VehicleId(vehicleId)))
                .map(vehicle -> Objects.equals(vehicle.getUserId().value(), currentUserId))
                .orElse(false);
    }

    /**
     * Checks if the currently authenticated user owns the specified profile.
     *
     * @param profileId      The profile UUID.
     * @param authentication Spring Security Authentication.
     * @return true if owner or admin, false otherwise.
     */
    public boolean isProfileOwner(UUID profileId, Authentication authentication) {
        if (isAdmin(authentication)) return true;
        String currentUserId = getUserIdFromAuthentication(authentication);
        if (currentUserId == null || profileId == null) return false;

        return profileQueryService.handle(new GetProfileByIdQuery(new ProfileId(profileId)))
                .map(profile -> Objects.equals(profile.getUserId().value(), currentUserId))
                .orElse(false);
    }

    /**
     * Checks if the currently authenticated user owns the specified simulation.
     *
     * @param simulationId   The simulation UUID.
     * @param authentication Spring Security Authentication.
     * @return true if owner or admin, false otherwise.
     */
    public boolean isSimulationOwner(UUID simulationId, Authentication authentication) {
        if (isAdmin(authentication)) return true;
        String currentUserId = getUserIdFromAuthentication(authentication);
        if (currentUserId == null || simulationId == null) return false;

        return simulationQueryService.handle(new GetSimulationByIdQuery(new SimulationId(simulationId)))
                .map(simulation -> Objects.equals(simulation.getUserId(), currentUserId))
                .orElse(false);
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        return SecurityUtils.getCurrentUserId()
                .orElseGet(() -> SecurityUtils.getCurrentUsername().orElse(null));
    }
}

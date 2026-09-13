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
import com.smartfinance.smartfinancedriveplatform.projections.application.queryservices.DepreciationProjectionQueryService;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.queries.GetDepreciationProjectionByIdQuery;
import com.smartfinance.smartfinancedriveplatform.projections.domain.model.valueobjects.ProjectionId;
import com.smartfinance.smartfinancedriveplatform.scoring.application.queryservices.CreditScoreQueryService;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.queries.GetCreditScoreByIdQuery;
import com.smartfinance.smartfinancedriveplatform.scoring.domain.model.valueobjects.ScoreId;
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
    private final CreditScoreQueryService creditScoreQueryService;
    private final DepreciationProjectionQueryService depreciationProjectionQueryService;

    public OwnershipChecker(VehicleQueryService vehicleQueryService,
                            ProfileQueryService profileQueryService,
                            SimulationQueryService simulationQueryService,
                            CreditScoreQueryService creditScoreQueryService,
                            DepreciationProjectionQueryService depreciationProjectionQueryService) {
        this.vehicleQueryService = vehicleQueryService;
        this.profileQueryService = profileQueryService;
        this.simulationQueryService = simulationQueryService;
        this.creditScoreQueryService = creditScoreQueryService;
        this.depreciationProjectionQueryService = depreciationProjectionQueryService;
    }

    /**
     * Checks if the target userId matches the authenticated user.
     */
    public boolean isUserSelf(Long userId, Authentication authentication) {
        if (isAdmin(authentication) || userId == null) return true;
        String currentUserId = getUserIdFromAuthentication(authentication);
        if (currentUserId != null) {
            return Objects.equals(currentUserId, userId.toString());
        }
        return Objects.equals(SecurityUtils.getCurrentUsername().orElse(null), userId.toString());
    }

    /**
     * Checks if the target userId string matches the authenticated user.
     */
    public boolean isUserSelfStr(String userIdStr, Authentication authentication) {
        if (isAdmin(authentication) || userIdStr == null || userIdStr.isBlank()) return true;
        String currentUserId = getUserIdFromAuthentication(authentication);
        if (currentUserId != null) {
            return Objects.equals(currentUserId, userIdStr);
        }
        return Objects.equals(SecurityUtils.getCurrentUsername().orElse(null), userIdStr);
    }

    /**
     * Checks if the currently authenticated user owns the specified vehicle.
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
     */
    public boolean isSimulationOwner(UUID simulationId, Authentication authentication) {
        if (isAdmin(authentication)) return true;
        String currentUserId = getUserIdFromAuthentication(authentication);
        if (currentUserId == null || simulationId == null) return false;

        return simulationQueryService.handle(new GetSimulationByIdQuery(new SimulationId(simulationId)))
                .map(simulation -> Objects.equals(simulation.getUserId(), currentUserId))
                .orElse(false);
    }

    /**
     * Checks if the currently authenticated user owns the profile related to the specified credit score.
     */
    public boolean isCreditScoreOwner(UUID scoreId, Authentication authentication) {
        if (isAdmin(authentication)) return true;
        String currentUserId = getUserIdFromAuthentication(authentication);
        if (currentUserId == null || scoreId == null) return false;

        return creditScoreQueryService.handle(new GetCreditScoreByIdQuery(new ScoreId(scoreId)))
                .map(score -> {
                    try {
                        UUID profileUUID = UUID.fromString(score.getProfileId());
                        return isProfileOwner(profileUUID, authentication);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    /**
     * Checks if the currently authenticated user owns the vehicle related to the specified depreciation projection.
     */
    public boolean isDepreciationProjectionOwner(UUID projectionId, Authentication authentication) {
        if (isAdmin(authentication)) return true;
        String currentUserId = getUserIdFromAuthentication(authentication);
        if (currentUserId == null || projectionId == null) return false;

        return depreciationProjectionQueryService.handle(new GetDepreciationProjectionByIdQuery(new ProjectionId(projectionId)))
                .map(projection -> {
                    try {
                        UUID vehicleUUID = UUID.fromString(projection.getVehicleId());
                        return isVehicleOwner(vehicleUUID, authentication);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) return null;
        return SecurityUtils.getCurrentUserId().orElse(null);
    }
}

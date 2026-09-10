package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.profiles.application.commandservices.ProfileCommandService;
import com.smartfinance.smartfinancedriveplatform.profiles.application.queryservices.ProfileQueryService;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands.DeleteProfileCommand;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.queries.GetProfileByIdQuery;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.queries.GetProfileByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.CreateProfileResource;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.ProfileResource;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.UpdateProfileResource;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.transform.CreateProfileCommandFromResourceAssembler;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.transform.ProfileResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.transform.UpdateProfileCommandFromResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing customer profiles.
 * Follows the CQRS pattern: delegates write commands to ProfileCommandService
 * and read queries to ProfileQueryService.
 */
@RestController
@RequestMapping(value = "/api/v1/profiles", produces = "application/json")
public class ProfilesController {

    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;

    public ProfilesController(ProfileCommandService profileCommandService, ProfileQueryService profileQueryService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
    }

    /**
     * POST /api/v1/profiles
     * Registers a new customer profile.
     *
     * @param resource The creation payload.
     * @return The created profile resource.
     */
    @PostMapping
    public ResponseEntity<ProfileResource> createProfile(@RequestBody CreateProfileResource resource) {
        var command = CreateProfileCommandFromResourceAssembler.toCommandFromResource(resource);
        var profileOpt = profileCommandService.handle(command);
        return profileOpt
                .map(profile -> new ResponseEntity<>(
                        ProfileResourceFromEntityAssembler.toResourceFromEntity(profile),
                        HttpStatus.CREATED
                ))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/v1/profiles/{profileId}
     * Retrieves profile details by profile ID.
     *
     * @param profileId The profile UUID.
     * @return The profile resource payload.
     */
    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResource> getProfileById(@PathVariable UUID profileId) {
        var query = new GetProfileByIdQuery(new ProfileId(profileId));
        var profileOpt = profileQueryService.handle(query);
        return profileOpt
                .map(profile -> ResponseEntity.ok(ProfileResourceFromEntityAssembler.toResourceFromEntity(profile)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/profiles/users/{userId}
     * Retrieves profile details by associated user ID.
     *
     * @param userId The user UUID.
     * @return The profile resource payload.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ProfileResource> getProfileByUserId(@PathVariable UUID userId) {
        var query = new GetProfileByUserIdQuery(new UserId(userId));
        var profileOpt = profileQueryService.handle(query);
        return profileOpt
                .map(profile -> ResponseEntity.ok(ProfileResourceFromEntityAssembler.toResourceFromEntity(profile)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/v1/profiles/{profileId}
     * Updates an existing customer profile details.
     *
     * @param profileId The profile UUID.
     * @param resource  The update payload.
     * @return The updated profile resource payload.
     */
    @PutMapping("/{profileId}")
    public ResponseEntity<ProfileResource> updateProfile(
            @PathVariable UUID profileId,
            @RequestBody UpdateProfileResource resource) {
        var command = UpdateProfileCommandFromResourceAssembler.toCommandFromResource(profileId, resource);
        var profileOpt = profileCommandService.handle(command);
        return profileOpt
                .map(profile -> ResponseEntity.ok(ProfileResourceFromEntityAssembler.toResourceFromEntity(profile)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/v1/profiles/{profileId}
     * Deletes a customer profile.
     *
     * @param profileId The profile UUID.
     * @return 204 No Content.
     */
    @DeleteMapping("/{profileId}")
    public ResponseEntity<?> deleteProfile(@PathVariable UUID profileId) {
        var command = new DeleteProfileCommand(new ProfileId(profileId));
        profileCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}

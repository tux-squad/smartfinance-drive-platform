package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.UserCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.application.internal.queryservices.UserQueryService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestDealerRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestFinancialInstitutionRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.UpdateUserRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetAllUsersQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetUserByIdQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.RequestDealerRoleResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.RequestFinancialInstitutionRoleResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.UpdateUserRoleResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.UserResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for User management queries and role management operations.
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UsersController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    public UsersController(UserQueryService userQueryService, UserCommandService userCommandService) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
    }

    /**
     * Retrieves paged list of registered users. Restricted to ADMIN role.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResource>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        Pageable cappedPageable = pageable;
        if (pageable.isPaged() && pageable.getPageSize() > 50) {
            cappedPageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), 50, pageable.getSort());
        }
        var usersPage = userQueryService.handle(new GetAllUsersQuery(), cappedPageable);
        var userResourcesPage = usersPage.map(UserResourceFromEntityAssembler::toResourceFromEntity);
        return ResponseEntity.ok(userResourcesPage);
    }

    /**
     * Retrieves a specific user by ID. Restricted to ADMIN role or self.
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipChecker.isUserSelf(#userId, authentication)")
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var user = userQueryService.handle(new GetUserByIdQuery(userId));
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }

    /**
     * Updates a user's security role. Restricted to ADMIN role.
     */
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResource> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleResource resource) {
        Roles role;
        try {
            role = Roles.valueOf(resource.role());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DomainValidationException("iam.error.role.invalid");
        }

        var command = new UpdateUserRoleCommand(userId, role);
        var updatedUserOpt = userCommandService.handle(command);
        return updatedUserOpt
                .map(user -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Requests automatic dealer role upgrade via SUNAT RUC verification. Restricted to self or ADMIN.
     */
    @PostMapping("/{userId}/dealer-role-requests")
    @PreAuthorize("hasRole('ADMIN') or @ownershipChecker.isUserSelf(#userId, authentication)")
    public ResponseEntity<UserResource> requestDealerRole(
            @PathVariable Long userId,
            @Valid @RequestBody RequestDealerRoleResource resource) {
        var command = new RequestDealerRoleCommand(userId, resource.ruc());
        var updatedUserOpt = userCommandService.handle(command);
        return updatedUserOpt
                .map(user -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Requests automatic financial institution role upgrade via SUNAT RUC verification. Restricted to self or ADMIN.
     */
    @PostMapping("/{userId}/financial-institution-role-requests")
    @PreAuthorize("hasRole('ADMIN') or @ownershipChecker.isUserSelf(#userId, authentication)")
    public ResponseEntity<UserResource> requestFinancialInstitutionRole(
            @PathVariable Long userId,
            @Valid @RequestBody RequestFinancialInstitutionRoleResource resource) {
        var command = new RequestFinancialInstitutionRoleCommand(userId, resource.ruc());
        var updatedUserOpt = userCommandService.handle(command);
        return updatedUserOpt
                .map(user -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

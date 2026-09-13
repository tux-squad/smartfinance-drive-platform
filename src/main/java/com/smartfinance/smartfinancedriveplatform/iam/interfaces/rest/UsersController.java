package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.queryservices.UserQueryService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetAllUsersQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetUserByIdQuery;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.UserResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for User management queries.
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UsersController {

    private final UserQueryService userQueryService;

    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    /**
     * Retrieves paged list of registered users. Restricted to ADMIN role.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResource>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        var usersPage = userQueryService.handle(new GetAllUsersQuery(), pageable);
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
}

package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.internal.queryservices.UserQueryService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetAllUsersQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetUserByIdQuery;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.UserResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsersController Unit Tests")
class UsersControllerTest {

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices.UserCommandService userCommandService;

    @InjectMocks
    private UsersController usersController;

    @Test
    @DisplayName("Should return list of users on getAllUsers")
    void shouldReturnAllUsers() {
        User user1 = new User(1L, new Username("john@example.com"), new Password("$2a$10$hashed1Password123"), List.of());
        User user2 = new User(2L, new Username("jane@example.com"), new Password("$2a$10$hashed2Password123"), List.of());

        when(userQueryService.handle(any(GetAllUsersQuery.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user1, user2)));

        ResponseEntity<Page<UserResource>> response = usersController.getAllUsers(Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("john@example.com", response.getBody().getContent().get(0).username());
    }

    @Test
    @DisplayName("Should return user resource on getUserById when found")
    void shouldReturnUserByIdWhenFound() {
        User user = new User(1L, new Username("john@example.com"), new Password("$2a$10$hashed1Password123"), List.of());

        when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.of(user));

        ResponseEntity<UserResource> response = usersController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("john@example.com", response.getBody().username());
    }

    @Test
    @DisplayName("Should return 404 Not Found on getUserById when not found")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.empty());

        ResponseEntity<UserResource> response = usersController.getUserById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Should return 200 OK on updateUserRole when valid")
    void shouldReturnOkOnUpdateUserRole() {
        User user = new User(1L, new Username("john@example.com"), new Password("$2a$10$hashed1Password123"), List.of(com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles.ROLE_DEALER));
        com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.UpdateUserRoleResource resource =
                new com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.UpdateUserRoleResource("ROLE_DEALER");

        when(userCommandService.handle(any(com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.UpdateUserRoleCommand.class))).thenReturn(Optional.of(user));

        ResponseEntity<UserResource> response = usersController.updateUserRole(1L, resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().roles().contains("ROLE_DEALER"));
    }

    @Test
    @DisplayName("Should return 200 OK on requestDealerRole when SUNAT verification passes")
    void shouldReturnOkOnRequestDealerRole() {
        User user = new User(1L, new Username("dealer@example.com"), new Password("$2a$10$hashed1Password123"), List.of(com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles.ROLE_DEALER));
        com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.RequestDealerRoleResource resource =
                new com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.RequestDealerRoleResource("20100128056");

        when(userCommandService.handle(any(com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestDealerRoleCommand.class))).thenReturn(Optional.of(user));

        ResponseEntity<UserResource> response = usersController.requestDealerRole(1L, resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().roles().contains("ROLE_DEALER"));
    }

    @Test
    @DisplayName("Should return 200 OK on requestFinancialInstitutionRole when SUNAT verification passes")
    void shouldReturnOkOnRequestFinancialInstitutionRole() {
        User user = new User(1L, new Username("finance@example.com"), new Password("$2a$10$hashed1Password123"), List.of(com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles.ROLE_FINANCIAL_INSTITUTION));
        com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.RequestFinancialInstitutionRoleResource resource =
                new com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.RequestFinancialInstitutionRoleResource("20100047218");

        when(userCommandService.handle(any(com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestFinancialInstitutionRoleCommand.class))).thenReturn(Optional.of(user));

        ResponseEntity<UserResource> response = usersController.requestFinancialInstitutionRole(1L, resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().roles().contains("ROLE_FINANCIAL_INSTITUTION"));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return 200 OK on initiateCorporateVerificationForMe")
    void shouldInitiateCorporateVerificationForMe() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "1", "pw", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                )
        );
        var resource = new com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.InitiateCorporateVerificationResource(
                "20100047218", "finanzas@viabcp.com"
        );
        var initiated = new com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationInitiated(
                "sess-123", true, "f***s@viabcp.com", 600
        );

        when(userCommandService.handle(any(com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.InitiateCorporateVerificationCommand.class)))
                .thenReturn(initiated);

        var response = usersController.initiateCorporateVerificationForMe(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("sess-123", response.getBody().sessionId());
        assertTrue(response.getBody().sessionActive());
        assertEquals("f***s@viabcp.com", response.getBody().maskedEmail());
        assertEquals(600, response.getBody().expiresInSeconds());
    }

    @Test
    @DisplayName("Should return 200 OK on confirmCorporateVerificationForMe")
    void shouldConfirmCorporateVerificationForMe() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "1", "pw", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                )
        );
        var resource = new com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.ConfirmCorporateVerificationResource(
                "20100047218", "123456"
        );
        var result = new com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationResult(
                true, "FINANCIAL_INSTITUTION", "ROLE_FINANCIAL_INSTITUTION", "bcp-id", "BANCO DE CREDITO DEL PERU", "Verification successful"
        );

        when(userCommandService.handle(any(com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ConfirmCorporateVerificationCommand.class)))
                .thenReturn(result);

        var response = usersController.confirmCorporateVerificationForMe(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().verified());
        assertEquals("ROLE_FINANCIAL_INSTITUTION", response.getBody().assignedRole());
        assertEquals("BANCO DE CREDITO DEL PERU", response.getBody().profileName());
    }

    @Test
    @DisplayName("Should return 200 OK on initiateCorporateVerification for target user")
    void shouldInitiateCorporateVerificationForTargetUser() {
        var resource = new com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.InitiateCorporateVerificationResource(
                "20100128056", "contacto@autoland.com.pe"
        );
        var initiated = new com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationInitiated(
                "sess-456", true, "c***o@autoland.com.pe", 600
        );

        when(userCommandService.handle(any(com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.InitiateCorporateVerificationCommand.class)))
                .thenReturn(initiated);

        var response = usersController.initiateCorporateVerification(2L, resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("sess-456", response.getBody().sessionId());
    }

    @Test
    @DisplayName("Should return 200 OK on confirmCorporateVerification for target user")
    void shouldConfirmCorporateVerificationForTargetUser() {
        var resource = new com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.ConfirmCorporateVerificationResource(
                "20100128056", "654321"
        );
        var result = new com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.CorporateVerificationResult(
                true, "DEALERSHIP", "ROLE_DEALER", "autoland-id", "AUTOLAND S.A.", "Verification successful"
        );

        when(userCommandService.handle(any(com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ConfirmCorporateVerificationCommand.class)))
                .thenReturn(result);

        var response = usersController.confirmCorporateVerification(2L, resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().verified());
        assertEquals("ROLE_DEALER", response.getBody().assignedRole());
    }
}

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

    @InjectMocks
    private UsersController usersController;

    @Test
    @DisplayName("Should return list of users on getAllUsers")
    void shouldReturnAllUsers() {
        User user1 = new User(1L, new Username("john@example.com"), new Password("hashed1"), List.of());
        User user2 = new User(2L, new Username("jane@example.com"), new Password("hashed2"), List.of());

        when(userQueryService.handle(any(GetAllUsersQuery.class))).thenReturn(List.of(user1, user2));

        ResponseEntity<List<UserResource>> response = usersController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("john@example.com", response.getBody().get(0).username());
    }

    @Test
    @DisplayName("Should return user resource on getUserById when found")
    void shouldReturnUserByIdWhenFound() {
        User user = new User(1L, new Username("john@example.com"), new Password("hashed1"), List.of());

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
}

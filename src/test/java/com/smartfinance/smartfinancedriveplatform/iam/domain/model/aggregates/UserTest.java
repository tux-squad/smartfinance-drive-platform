package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Aggregate Root Unit Tests")
class UserTest {

    @Test
    @DisplayName("Should create user successfully with valid username and password")
    void shouldCreateUserSuccessfully() {
        Username username = new Username("john.doe@example.com");
        Password password = new Password("secretPassword123");

        User user = new User(username, password);

        assertNotNull(user);
        assertEquals("john.doe@example.com", user.getUsername().username());
        assertEquals("secretPassword123", user.getPassword().password());
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(Roles.ROLE_USER));
    }

    @Test
    @DisplayName("Should throw exception when username format is invalid")
    void shouldThrowExceptionWhenUsernameInvalid() {
        assertThrows(DomainValidationException.class, () -> new Username("invalid-email-string"));
        assertThrows(DomainValidationException.class, () -> new Username(""));
        assertThrows(DomainValidationException.class, () -> new Username(null));
    }

    @Test
    @DisplayName("Should throw exception when password is blank or null")
    void shouldThrowExceptionWhenPasswordInvalid() {
        assertThrows(DomainValidationException.class, () -> new Password(""));
        assertThrows(DomainValidationException.class, () -> new Password(null));
    }

    @Test
    @DisplayName("Should allow adding and removing roles")
    void shouldAddAndRemoveRoles() {
        User user = new User(new Username("admin@smartfinance.com"), new Password("pass123"));

        user.addRole(Roles.ROLE_ADMIN);
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(Roles.ROLE_ADMIN));

        user.removeRole(Roles.ROLE_ADMIN);
        assertEquals(1, user.getRoles().size());
        assertFalse(user.getRoles().contains(Roles.ROLE_ADMIN));
    }
}

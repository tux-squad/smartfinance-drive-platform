package com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.profiles.application.commandservices.ProfileCommandService;
import com.smartfinance.smartfinancedriveplatform.profiles.application.queryservices.ProfileQueryService;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates.Profile;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.commands.CreateProfileCommand;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.queries.GetProfileByIdQuery;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.queries.GetProfileByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.CreateProfileResource;
import com.smartfinance.smartfinancedriveplatform.profiles.interfaces.rest.resources.ProfileResource;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProfilesController.
 * Mocks application layer services using Mockito to test mapping and REST handlers.
 */
@ExtendWith(MockitoExtension.class)
class ProfilesControllerTest {

    @Mock
    private ProfileCommandService profileCommandService;

    @Mock
    private ProfileQueryService profileQueryService;

    private ProfilesController profilesController;

    @BeforeEach
    void setUp() {
        profilesController = new ProfilesController(profileCommandService, profileQueryService);
    }

    @Test
    void testCreateProfileSuccess() {
        UUID userId = UUID.randomUUID();
        CreateProfileResource resource = new CreateProfileResource(
            userId,
            "john.doe@example.com",
            "71234567",
            "John Doe",
            LocalDate.of(1990, 5, 15),
            "+51",
            "987654321",
            BigDecimal.valueOf(3500),
            "PEN",
            "EMPLOYED"
        );

        Profile profile = new Profile(
            new UserId(userId),
            "john.doe@example.com",
            "71234567",
            "John Doe",
            LocalDate.of(1990, 5, 15),
            "+51",
            "987654321",
            Money.of(3500, "PEN"),
            "EMPLOYED"
        );

        when(profileCommandService.handle(any(CreateProfileCommand.class))).thenReturn(Optional.of(profile));

        ResponseEntity<ProfileResource> response = profilesController.createProfile(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe@example.com", response.getBody().email());
        assertEquals("John Doe", response.getBody().fullLegalNames());
    }

    @Test
    void testGetProfileByIdFound() {
        UUID profileId = UUID.randomUUID();
        Profile profile = new Profile(
            new UserId(UUID.randomUUID()),
            "john.doe@example.com",
            "71234567",
            "John Doe",
            LocalDate.of(1990, 5, 15),
            "+51",
            "987654321",
            Money.of(3500, "PEN"),
            "EMPLOYED"
        );

        when(profileQueryService.handle(any(GetProfileByIdQuery.class))).thenReturn(Optional.of(profile));

        ResponseEntity<ProfileResource> response = profilesController.getProfileById(profileId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe@example.com", response.getBody().email());
    }

    @Test
    void testGetProfileByUserIdFound() {
        UUID userId = UUID.randomUUID();
        Profile profile = new Profile(
            new UserId(userId),
            "john.doe@example.com",
            "71234567",
            "John Doe",
            LocalDate.of(1990, 5, 15),
            "+51",
            "987654321",
            Money.of(3500, "PEN"),
            "EMPLOYED"
        );

        when(profileQueryService.handle(any(GetProfileByUserIdQuery.class))).thenReturn(Optional.of(profile));

        ResponseEntity<ProfileResource> response = profilesController.getProfileByUserId(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().userId());
    }
}

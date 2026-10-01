package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.FinancialEntityCommandService;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetAllFinancialEntitiesQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CreateFinancialEntityResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.FinancialEntityResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FinancialEntitiesController.
 * Mocks application layer services using Mockito to test mapping and REST handlers.
 */
@ExtendWith(MockitoExtension.class)
class FinancialEntitiesControllerTest {

    @Mock
    private FinancialEntityCommandService financialEntityCommandService;

    @Mock
    private FinancialEntityQueryService financialEntityQueryService;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.OwnershipChecker ownershipChecker;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.storage.FinancialEntityImageStorageService imageStorageService;

    private FinancialEntitiesController financialEntitiesController;

    @BeforeEach
    void setUp() {
        financialEntitiesController = new FinancialEntitiesController(financialEntityCommandService, financialEntityQueryService, ownershipChecker, imageStorageService);
    }

    @Test
    void testCreateFinancialEntitySuccess() {
        CreateFinancialEntityResource resource = new CreateFinancialEntityResource("BCP");
        FinancialEntity entity = new FinancialEntity("BCP");

        when(financialEntityCommandService.handle(any(CreateFinancialEntityCommand.class))).thenReturn(Optional.of(entity));

        ResponseEntity<FinancialEntityResource> response = financialEntitiesController.createFinancialEntity(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BCP", response.getBody().name());
    }

    @Test
    void testCreateFinancialEntityWithLogoAndBanner() {
        CreateFinancialEntityResource resource = new CreateFinancialEntityResource("BBVA", "https://cdn.example.com/bbva-logo.png", "https://cdn.example.com/bbva-banner.png");
        FinancialEntity entity = new FinancialEntity("BBVA", "https://cdn.example.com/bbva-logo.png", "https://cdn.example.com/bbva-banner.png");

        when(financialEntityCommandService.handle(any(CreateFinancialEntityCommand.class))).thenReturn(Optional.of(entity));

        ResponseEntity<FinancialEntityResource> response = financialEntitiesController.createFinancialEntity(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BBVA", response.getBody().name());
        assertEquals("https://cdn.example.com/bbva-logo.png", response.getBody().logoUrl());
        assertEquals("https://cdn.example.com/bbva-banner.png", response.getBody().bannerUrl());
    }

    @Test
    void testCreateFinancialEntityResourceInvalidUrlValidation() {
        var validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        CreateFinancialEntityResource resource = new CreateFinancialEntityResource(null, "20100047218", "Banco BCP", "invalid-url", "ftp://invalid-banner");
        var violations = validator.validate(resource);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("logoUrl")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("bannerUrl")));
    }

    @Test
    void testGetFinancialEntityByIdFound() {
        UUID id = UUID.randomUUID();
        FinancialEntity entity = new FinancialEntity("Interbank");

        when(financialEntityQueryService.handle(any(GetFinancialEntityByIdQuery.class))).thenReturn(Optional.of(entity));

        ResponseEntity<FinancialEntityResource> response = financialEntitiesController.getFinancialEntityById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Interbank", response.getBody().name());
    }

    @Test
    void testGetAllFinancialEntities() {
        when(financialEntityQueryService.handle(any(GetAllFinancialEntitiesQuery.class))).thenReturn(Collections.emptyList());

        ResponseEntity<List<FinancialEntityResource>> response = financialEntitiesController.getAllFinancialEntities();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetMyFinancialEntity() {
        String authUserId = "bank-user-99";
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                authUserId, "pwd", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_FINANCIAL_INSTITUTION"))
        );
        auth.setDetails(new com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils.AuthenticatedUserDetails(authUserId, authUserId));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        FinancialEntity entity = new FinancialEntity(
                new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(UUID.randomUUID()),
                authUserId,
                "Mi Banco",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityQueryService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByUserIdQuery.class)))
                .thenReturn(Optional.of(entity));

        try {
            ResponseEntity<FinancialEntityResource> response = financialEntitiesController.getMyFinancialEntity();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Mi Banco", response.getBody().name());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testUploadMyLogoSuccess() {
        String authUserId = "bank-user-99";
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                authUserId, "pwd", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_FINANCIAL_INSTITUTION"))
        );
        auth.setDetails(new com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils.AuthenticatedUserDetails(authUserId, authUserId));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        UUID entityUuid = UUID.randomUUID();
        var entityId = new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(entityUuid);
        FinancialEntity entity = new FinancialEntity(entityId, authUserId, "20100047218", "Banco BCP", "old-logo.png", null, Collections.emptyList());
        FinancialEntity updatedEntity = new FinancialEntity(entityId, authUserId, "20100047218", "Banco BCP", "https://cdn.example.com/new-logo.png", null, Collections.emptyList());

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "logo.png", "image/png", "sample".getBytes());

        when(financialEntityQueryService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByUserIdQuery.class)))
                .thenReturn(Optional.of(entity));
        when(imageStorageService.uploadFinancialEntityImage(eq(file), eq("logos")))
                .thenReturn("https://cdn.example.com/new-logo.png");
        when(financialEntityCommandService.updateLogo(eq(entityId), eq("https://cdn.example.com/new-logo.png")))
                .thenReturn(Optional.of(updatedEntity));

        try {
            ResponseEntity<FinancialEntityResource> response = financialEntitiesController.uploadMyLogo(file);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("https://cdn.example.com/new-logo.png", response.getBody().logoUrl());

            var inOrder = Mockito.inOrder(imageStorageService, financialEntityCommandService);
            inOrder.verify(imageStorageService).uploadFinancialEntityImage(eq(file), eq("logos"));
            inOrder.verify(financialEntityCommandService).updateLogo(eq(entityId), eq("https://cdn.example.com/new-logo.png"));
            inOrder.verify(imageStorageService).deleteFinancialEntityImage("old-logo.png");
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testUploadMyBannerSuccess() {
        String authUserId = "bank-user-99";
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                authUserId, "pwd", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_FINANCIAL_INSTITUTION"))
        );
        auth.setDetails(new com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils.AuthenticatedUserDetails(authUserId, authUserId));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        UUID entityUuid = UUID.randomUUID();
        var entityId = new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(entityUuid);
        FinancialEntity entity = new FinancialEntity(entityId, authUserId, "20100047218", "Banco BCP", null, "old-banner.png", Collections.emptyList());
        FinancialEntity updatedEntity = new FinancialEntity(entityId, authUserId, "20100047218", "Banco BCP", null, "https://cdn.example.com/new-banner.png", Collections.emptyList());

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "banner.png", "image/png", "sample".getBytes());

        when(financialEntityQueryService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByUserIdQuery.class)))
                .thenReturn(Optional.of(entity));
        when(imageStorageService.uploadFinancialEntityImage(eq(file), eq("banners")))
                .thenReturn("https://cdn.example.com/new-banner.png");
        when(financialEntityCommandService.updateBanner(eq(entityId), eq("https://cdn.example.com/new-banner.png")))
                .thenReturn(Optional.of(updatedEntity));

        try {
            ResponseEntity<FinancialEntityResource> response = financialEntitiesController.uploadMyBanner(file);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("https://cdn.example.com/new-banner.png", response.getBody().bannerUrl());

            var inOrder = Mockito.inOrder(imageStorageService, financialEntityCommandService);
            inOrder.verify(imageStorageService).uploadFinancialEntityImage(eq(file), eq("banners"));
            inOrder.verify(financialEntityCommandService).updateBanner(eq(entityId), eq("https://cdn.example.com/new-banner.png"));
            inOrder.verify(imageStorageService).deleteFinancialEntityImage("old-banner.png");
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testUploadLogoByIdSuccess() {
        UUID entityUuid = UUID.randomUUID();
        var entityId = new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(entityUuid);
        FinancialEntity entity = new FinancialEntity(entityId, "bank-user-99", "20100047218", "Banco BCP", "old-logo.png", null, Collections.emptyList());
        FinancialEntity updatedEntity = new FinancialEntity(entityId, "bank-user-99", "20100047218", "Banco BCP", "https://cdn.example.com/new-logo.png", null, Collections.emptyList());

        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin-user", "pwd", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "logo.png", "image/png", "sample".getBytes());

        when(financialEntityQueryService.handle(any(GetFinancialEntityByIdQuery.class))).thenReturn(Optional.of(entity));
        when(imageStorageService.uploadFinancialEntityImage(eq(file), eq("logos"))).thenReturn("https://cdn.example.com/new-logo.png");
        when(financialEntityCommandService.updateLogo(eq(entityId), eq("https://cdn.example.com/new-logo.png"))).thenReturn(Optional.of(updatedEntity));

        try {
            ResponseEntity<FinancialEntityResource> response = financialEntitiesController.uploadLogo(entityUuid, file);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("https://cdn.example.com/new-logo.png", response.getBody().logoUrl());

            var inOrder = Mockito.inOrder(imageStorageService, financialEntityCommandService);
            inOrder.verify(imageStorageService).uploadFinancialEntityImage(eq(file), eq("logos"));
            inOrder.verify(financialEntityCommandService).updateLogo(eq(entityId), eq("https://cdn.example.com/new-logo.png"));
            inOrder.verify(imageStorageService).deleteFinancialEntityImage("old-logo.png");
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testUploadBannerByIdSuccess() {
        UUID entityUuid = UUID.randomUUID();
        var entityId = new com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId(entityUuid);
        FinancialEntity entity = new FinancialEntity(entityId, "bank-user-99", "20100047218", "Banco BCP", null, "old-banner.png", Collections.emptyList());
        FinancialEntity updatedEntity = new FinancialEntity(entityId, "bank-user-99", "20100047218", "Banco BCP", null, "https://cdn.example.com/new-banner.png", Collections.emptyList());

        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin-user", "pwd", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "banner.png", "image/png", "sample".getBytes());

        when(financialEntityQueryService.handle(any(GetFinancialEntityByIdQuery.class))).thenReturn(Optional.of(entity));
        when(imageStorageService.uploadFinancialEntityImage(eq(file), eq("banners"))).thenReturn("https://cdn.example.com/new-banner.png");
        when(financialEntityCommandService.updateBanner(eq(entityId), eq("https://cdn.example.com/new-banner.png"))).thenReturn(Optional.of(updatedEntity));

        try {
            ResponseEntity<FinancialEntityResource> response = financialEntitiesController.uploadBanner(entityUuid, file);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("https://cdn.example.com/new-banner.png", response.getBody().bannerUrl());

            var inOrder = Mockito.inOrder(imageStorageService, financialEntityCommandService);
            inOrder.verify(imageStorageService).uploadFinancialEntityImage(eq(file), eq("banners"));
            inOrder.verify(financialEntityCommandService).updateBanner(eq(entityId), eq("https://cdn.example.com/new-banner.png"));
            inOrder.verify(imageStorageService).deleteFinancialEntityImage("old-banner.png");
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}

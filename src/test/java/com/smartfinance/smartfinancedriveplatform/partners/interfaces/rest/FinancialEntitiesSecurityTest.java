package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.FinancialEntityCommandService;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.FinancialEntityQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.AddRateBenchmarkCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.UpdateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.AddRateBenchmarkResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CreateFinancialEntityResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.FinancialEntityResource;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.UpdateFinancialEntityResource;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.OwnershipChecker;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialEntities Security & Authorization Tests")
class FinancialEntitiesSecurityTest {

    @Mock
    private FinancialEntityCommandService financialEntityCommandService;

    @Mock
    private FinancialEntityQueryService financialEntityQueryService;

    @Mock
    private OwnershipChecker ownershipChecker;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.storage.FinancialEntityImageStorageService imageStorageService;

    @InjectMocks
    private FinancialEntitiesController controller;

    private final UUID entityId = UUID.randomUUID();
    private final String authUserId = "fi-user-1";
    private final String victimUserId = "fi-user-2";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String userId, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, "credentials", List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails(userId, userId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("PUT /{id}: FI cannot update financial entity they do not own (Hijack prevention)")
    void fiCannotUpdateEntityIfNotOwner() {
        authenticateAs(authUserId, "FINANCIAL_INSTITUTION");
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(false);

        UpdateFinancialEntityResource resource = new UpdateFinancialEntityResource("Malicious Rename", null, null);

        var ex = assertThrows(AccessDeniedException.class, () ->
                controller.updateFinancialEntity(entityId, resource));

        assertEquals("partners.error.accessDenied.notOwner", ex.getMessage());
        verify(financialEntityCommandService, never()).handle(any(UpdateFinancialEntityCommand.class));
    }

    @Test
    @DisplayName("PUT /{id}: FI owner cannot pass resource.userId to reassign ownership")
    void fiCannotReassignOwnershipEvenIfOwner() {
        authenticateAs(authUserId, "FINANCIAL_INSTITUTION");
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(true);

        UpdateFinancialEntityResource resource = new UpdateFinancialEntityResource("attacker-id", "20100047218", "Bank Name", null, null);

        var ex = assertThrows(AccessDeniedException.class, () ->
                controller.updateFinancialEntity(entityId, resource));

        assertEquals("partners.error.accessDenied.cannotTransferOwnership", ex.getMessage());
        verify(financialEntityCommandService, never()).handle(any(UpdateFinancialEntityCommand.class));
    }

    @Test
    @DisplayName("PUT /{id}: FI owner can update details of their entity")
    void fiOwnerCanUpdateEntity() {
        authenticateAs(authUserId, "FINANCIAL_INSTITUTION");
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(true);

        FinancialEntity updatedEntity = new FinancialEntity(
                new FinancialEntityId(entityId),
                authUserId,
                "20100047218",
                "Bank Updated",
                "https://cdn.example.com/new-logo.png",
                null,
                Collections.emptyList()
        );
        when(financialEntityCommandService.handle(any(UpdateFinancialEntityCommand.class))).thenReturn(Optional.of(updatedEntity));

        UpdateFinancialEntityResource resource = new UpdateFinancialEntityResource("Bank Updated", "https://cdn.example.com/new-logo.png", null);
        ResponseEntity<FinancialEntityResource> response = controller.updateFinancialEntity(entityId, resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bank Updated", response.getBody().name());
    }

    @Test
    @DisplayName("PUT /{id}: ADMIN can update entity and explicitly set/transfer userId")
    void adminCanUpdateEntityAndReassignUserId() {
        authenticateAs("admin-user", "ADMIN");

        FinancialEntity updatedEntity = new FinancialEntity(
                new FinancialEntityId(entityId),
                victimUserId,
                "20100047218",
                "Bank Reassigned",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityCommandService.handle(any(UpdateFinancialEntityCommand.class))).thenReturn(Optional.of(updatedEntity));

        UpdateFinancialEntityResource resource = new UpdateFinancialEntityResource(victimUserId, "20100047218", "Bank Reassigned", null, null);
        ResponseEntity<FinancialEntityResource> response = controller.updateFinancialEntity(entityId, resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bank Reassigned", response.getBody().name());
        verify(financialEntityCommandService).handle(argThat((UpdateFinancialEntityCommand cmd) -> victimUserId.equals(cmd.userId())));
    }

    @Test
    @DisplayName("POST /{id}/rate-benchmarks: FI cannot inject rate benchmarks into entity they do not own")
    void fiCannotInjectBenchmarksIfNotOwner() {
        authenticateAs(authUserId, "FINANCIAL_INSTITUTION");
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(false);

        AddRateBenchmarkResource resource = new AddRateBenchmarkResource("TEA", BigDecimal.valueOf(12.5), "PEN", "SBS", "https://sbs.gob.pe", LocalDate.now());

        var ex = assertThrows(AccessDeniedException.class, () ->
                controller.addRateBenchmark(entityId, resource));

        assertEquals("partners.error.accessDenied.notOwner", ex.getMessage());
        verify(financialEntityCommandService, never()).handle(any(AddRateBenchmarkCommand.class));
    }

    @Test
    @DisplayName("POST /{id}/rate-benchmarks: FI owner can add rate benchmarks to their entity")
    void fiOwnerCanAddBenchmarks() {
        authenticateAs(authUserId, "FINANCIAL_INSTITUTION");
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(true);

        FinancialEntity entity = new FinancialEntity(
                new FinancialEntityId(entityId),
                authUserId,
                "20100047218",
                "Bank",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityCommandService.handle(any(AddRateBenchmarkCommand.class))).thenReturn(Optional.of(entity));

        AddRateBenchmarkResource resource = new AddRateBenchmarkResource("TEA", BigDecimal.valueOf(12.5), "PEN", "SBS", "https://sbs.gob.pe", LocalDate.now());
        ResponseEntity<FinancialEntityResource> response = controller.addRateBenchmark(entityId, resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(financialEntityCommandService).handle(any(AddRateBenchmarkCommand.class));
    }

    @Test
    @DisplayName("POST /: FI cannot impersonate another userId when creating entity")
    void fiCannotImpersonateAnotherUserIdInCreate() {
        authenticateAs(authUserId, "FINANCIAL_INSTITUTION");

        CreateFinancialEntityResource resource = new CreateFinancialEntityResource(victimUserId, "20100047218", "Impersonated Bank", null, null);

        var ex = assertThrows(AccessDeniedException.class, () ->
                controller.createFinancialEntity(resource));

        assertEquals("partners.error.accessDenied.cannotImpersonateUserId", ex.getMessage());
        verify(financialEntityCommandService, never()).handle(any(CreateFinancialEntityCommand.class));
    }

    @Test
    @DisplayName("POST /: FI creating entity automatically binds to own authUserId")
    void fiCreatingEntityBindsToOwnUserId() {
        authenticateAs(authUserId, "FINANCIAL_INSTITUTION");

        FinancialEntity entity = new FinancialEntity(
                new FinancialEntityId(entityId),
                authUserId,
                "20100047218",
                "My Bank",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityCommandService.handle(any(CreateFinancialEntityCommand.class))).thenReturn(Optional.of(entity));

        CreateFinancialEntityResource resource = new CreateFinancialEntityResource(null, "20100047218", "My Bank", null, null);
        ResponseEntity<FinancialEntityResource> response = controller.createFinancialEntity(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(financialEntityCommandService).handle(argThat((CreateFinancialEntityCommand cmd) -> authUserId.equals(cmd.userId())));
    }

    @Test
    @DisplayName("POST /: ADMIN can create entity with arbitrary userId")
    void adminCanCreateEntityWithArbitraryUserId() {
        authenticateAs("admin-user", "ADMIN");

        FinancialEntity entity = new FinancialEntity(
                new FinancialEntityId(entityId),
                victimUserId,
                "20100047218",
                "Victim Bank",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityCommandService.handle(any(CreateFinancialEntityCommand.class))).thenReturn(Optional.of(entity));

        CreateFinancialEntityResource resource = new CreateFinancialEntityResource(victimUserId, "20100047218", "Victim Bank", null, null);
        ResponseEntity<FinancialEntityResource> response = controller.createFinancialEntity(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(financialEntityCommandService).handle(argThat((CreateFinancialEntityCommand cmd) -> victimUserId.equals(cmd.userId())));
    }

    @Test
    @DisplayName("GET /{id}: regular USER sees sanitized resource with userId = null")
    void regularUserGetsSanitizedEntityWithoutUserId() {
        authenticateAs("regular-user-42", "USER");

        FinancialEntity entity = new FinancialEntity(
                new FinancialEntityId(entityId),
                "internal-bank-owner-id",
                "20100047218",
                "Public Bank",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityQueryService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery.class)))
                .thenReturn(Optional.of(entity));

        ResponseEntity<FinancialEntityResource> response = controller.getFinancialEntityById(entityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Public Bank", response.getBody().name());
        assertNull(response.getBody().userId(), "Internal userId must be sanitized to null for third parties");
    }

    @Test
    @DisplayName("GET /{id}: owner sees their own userId")
    void ownerGetsEntityWithUserIdPopulated() {
        authenticateAs("owner-user-77", "FINANCIAL_INSTITUTION");

        FinancialEntity entity = new FinancialEntity(
                new FinancialEntityId(entityId),
                "owner-user-77",
                "20100047218",
                "My Bank",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityQueryService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery.class)))
                .thenReturn(Optional.of(entity));

        ResponseEntity<FinancialEntityResource> response = controller.getFinancialEntityById(entityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("owner-user-77", response.getBody().userId());
    }

    @Test
    @DisplayName("GET /{id}: ADMIN sees userId populated for any entity")
    void adminGetsEntityWithUserIdPopulated() {
        authenticateAs("admin-user-1", "ADMIN");

        FinancialEntity entity = new FinancialEntity(
                new FinancialEntityId(entityId),
                "internal-bank-owner-id",
                "20100047218",
                "Any Bank",
                null,
                null,
                Collections.emptyList()
        );
        when(financialEntityQueryService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery.class)))
                .thenReturn(Optional.of(entity));

        ResponseEntity<FinancialEntityResource> response = controller.getFinancialEntityById(entityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("internal-bank-owner-id", response.getBody().userId());
    }

    @Test
    @DisplayName("POST /{id}/logo: FI cannot upload logo for entity they do not own")
    void uploadLogoForbiddenIfNotOwner() {
        authenticateAs(authUserId, "FINANCIAL_INSTITUTION");
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(false);

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "logo.png", "image/png", "sample".getBytes());

        var ex = assertThrows(AccessDeniedException.class, () -> controller.uploadLogo(entityId, file));
        assertEquals("partners.error.accessDenied.notOwner", ex.getMessage());
        verifyNoInteractions(imageStorageService);
    }

    @Test
    @DisplayName("POST /{id}/banner: FI cannot upload banner for entity they do not own")
    void uploadBannerForbiddenIfNotOwner() {
        authenticateAs(authUserId, "FINANCIAL_INSTITUTION");
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(false);

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "banner.png", "image/png", "sample".getBytes());

        var ex = assertThrows(AccessDeniedException.class, () -> controller.uploadBanner(entityId, file));
        assertEquals("partners.error.accessDenied.notOwner", ex.getMessage());
        verifyNoInteractions(imageStorageService);
    }

    @Test
    @DisplayName("POST /{id}/logo: FI owner can upload logo")
    void uploadLogoAllowedIfOwner() {
        authenticateAs(authUserId, "FINANCIAL_INSTITUTION");
        when(ownershipChecker.isFinancialEntityOwner(eq(entityId), any())).thenReturn(true);

        FinancialEntity entity = new FinancialEntity(new FinancialEntityId(entityId), authUserId, "20100047218", "My Bank", null, null, Collections.emptyList());
        FinancialEntity updatedEntity = new FinancialEntity(new FinancialEntityId(entityId), authUserId, "20100047218", "My Bank", "https://cdn.example.com/logo.png", null, Collections.emptyList());

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "logo.png", "image/png", "sample".getBytes());

        when(financialEntityQueryService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery.class)))
                .thenReturn(Optional.of(entity));
        when(imageStorageService.uploadFinancialEntityImage(eq(file), eq("logos"))).thenReturn("https://cdn.example.com/logo.png");
        when(financialEntityCommandService.updateLogo(eq(new FinancialEntityId(entityId)), eq("https://cdn.example.com/logo.png")))
                .thenReturn(Optional.of(updatedEntity));

        var response = controller.uploadLogo(entityId, file);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("https://cdn.example.com/logo.png", response.getBody().logoUrl());
    }

    @Test
    @DisplayName("POST /{id}/banner: ADMIN can upload banner even if not owner")
    void uploadBannerAllowedIfAdmin() {
        authenticateAs("admin-user-1", "ADMIN");

        FinancialEntity entity = new FinancialEntity(new FinancialEntityId(entityId), authUserId, "20100047218", "Any Bank", null, null, Collections.emptyList());
        FinancialEntity updatedEntity = new FinancialEntity(new FinancialEntityId(entityId), authUserId, "20100047218", "Any Bank", null, "https://cdn.example.com/banner.png", Collections.emptyList());

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "banner.png", "image/png", "sample".getBytes());

        when(financialEntityQueryService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetFinancialEntityByIdQuery.class)))
                .thenReturn(Optional.of(entity));
        when(imageStorageService.uploadFinancialEntityImage(eq(file), eq("banners"))).thenReturn("https://cdn.example.com/banner.png");
        when(financialEntityCommandService.updateBanner(eq(new FinancialEntityId(entityId)), eq("https://cdn.example.com/banner.png")))
                .thenReturn(Optional.of(updatedEntity));

        var response = controller.uploadBanner(entityId, file);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("https://cdn.example.com/banner.png", response.getBody().bannerUrl());
    }
}

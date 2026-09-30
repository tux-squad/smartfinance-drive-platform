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
import org.mockito.Mock;
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

    private FinancialEntitiesController financialEntitiesController;

    @BeforeEach
    void setUp() {
        financialEntitiesController = new FinancialEntitiesController(financialEntityCommandService, financialEntityQueryService, ownershipChecker);
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
}

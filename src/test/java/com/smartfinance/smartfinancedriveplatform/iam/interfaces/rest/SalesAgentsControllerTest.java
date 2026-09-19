package com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.iam.application.commandservices.SalesAgentCommandService;
import com.smartfinance.smartfinancedriveplatform.iam.application.queryservices.SalesAgentQueryService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.SalesAgent;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.CreateSalesAgentCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ReassignLeadsCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.UpdateSalesAgentCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.queries.GetSalesAgentsForDealerQuery;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.CreateUpdateSalesAgentResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.ReassignLeadsResource;
import com.smartfinance.smartfinancedriveplatform.iam.interfaces.rest.resources.SalesAgentResource;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesAgentsController REST Unit Tests")
class SalesAgentsControllerTest {

    @Mock
    private SalesAgentCommandService commandService;

    @Mock
    private SalesAgentQueryService queryService;

    @InjectMocks
    private SalesAgentsController controller;

    private SalesAgent sampleAgent;

    @BeforeEach
    void setUp() {
        sampleAgent = new SalesAgent("dealer-123", "Carlos Gomez", "carlos@dealer.com", "999111222");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "dealer-123", "password", Collections.emptyList()
        );
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails("dealer-123", "dealer-123"));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should get my sales agents and return 200 OK")
    void shouldGetMySalesAgents() {
        when(queryService.handle(any(GetSalesAgentsForDealerQuery.class))).thenReturn(List.of(sampleAgent));

        ResponseEntity<List<SalesAgentResource>> response = controller.getMySalesAgents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Carlos Gomez", response.getBody().get(0).fullName());
    }

    @Test
    @DisplayName("Should create sales agent and return 201 Created")
    void shouldCreateSalesAgent() {
        CreateUpdateSalesAgentResource resource = new CreateUpdateSalesAgentResource("Carlos Gomez", "carlos@dealer.com", "999111222", true);
        when(commandService.handle(any(CreateSalesAgentCommand.class))).thenReturn(sampleAgent);

        ResponseEntity<SalesAgentResource> response = controller.createSalesAgent(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Carlos Gomez", response.getBody().fullName());
    }

    @Test
    @DisplayName("Should update sales agent and return 200 OK")
    void shouldUpdateSalesAgent() {
        CreateUpdateSalesAgentResource resource = new CreateUpdateSalesAgentResource("Carlos Gomez", "carlos@dealer.com", "999111222", true);
        when(commandService.handle(any(UpdateSalesAgentCommand.class))).thenReturn(sampleAgent);

        ResponseEntity<SalesAgentResource> response = controller.updateSalesAgent(UUID.randomUUID(), resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should reassign leads and return 204 No Content")
    void shouldReassignLeads() {
        ReassignLeadsResource resource = new ReassignLeadsResource(UUID.randomUUID());
        doNothing().when(commandService).handle(any(ReassignLeadsCommand.class));

        ResponseEntity<Void> response = controller.reassignLeads(UUID.randomUUID(), resource);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commandService, times(1)).handle(any(ReassignLeadsCommand.class));
    }
}

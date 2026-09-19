package com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.crm.application.commandservices.CrmCommandService;
import com.smartfinance.smartfinancedriveplatform.crm.application.queryservices.CrmQueryService;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.Prospect;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.aggregates.TestDrive;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.commands.*;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.entities.ProspectNote;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetProspectByIdQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetProspectsForDealerQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetTestDriveByIdQuery;
import com.smartfinance.smartfinancedriveplatform.crm.domain.model.queries.GetTestDrivesForUserQuery;
import com.smartfinance.smartfinancedriveplatform.crm.interfaces.rest.resources.*;
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

import java.time.LocalDateTime;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrmController REST Unit Tests")
class CrmControllerTest {

    @Mock
    private CrmCommandService commandService;

    @Mock
    private CrmQueryService queryService;

    @InjectMocks
    private CrmController crmController;

    private Prospect sampleProspect;
    private TestDrive sampleTestDrive;

    @BeforeEach
    void setUp() {
        sampleProspect = new Prospect("dealer-123", "buyer-456", "Juan Perez", "juan@gmail.com", "999888777", UUID.randomUUID(), "agent-1");
        sampleTestDrive = new TestDrive("buyer-456", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), "Drive notes");

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
    @DisplayName("Should create prospect and return 201 Created")
    void shouldCreateProspect() {
        CreateProspectResource resource = new CreateProspectResource("Juan Perez", "juan@gmail.com", "999888777", UUID.randomUUID(), "agent-1");
        when(commandService.handle(any(CreateProspectCommand.class))).thenReturn(Optional.of(sampleProspect));

        ResponseEntity<ProspectResource> response = crmController.createProspect(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Juan Perez", response.getBody().fullName());
    }

    @Test
    @DisplayName("Should get prospects for dealer and return 200 OK")
    void shouldGetMyProspects() {
        when(queryService.handle(any(GetProspectsForDealerQuery.class))).thenReturn(List.of(sampleProspect));

        ResponseEntity<List<ProspectResource>> response = crmController.getMyProspects();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should get prospect by ID")
    void shouldGetProspectById() {
        when(queryService.handle(any(GetProspectByIdQuery.class))).thenReturn(Optional.of(sampleProspect));

        ResponseEntity<ProspectResource> response = crmController.getProspectById(UUID.randomUUID());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should add prospect note and return 201 Created")
    void shouldAddProspectNote() {
        ProspectNote note = new ProspectNote(sampleProspect.getId().value(), "dealer-123", "Test note text");
        AddProspectNoteResource resource = new AddProspectNoteResource("Test note text");
        when(commandService.handle(any(AddProspectNoteCommand.class))).thenReturn(Optional.of(note));

        ResponseEntity<ProspectNoteResource> response = crmController.addProspectNote(UUID.randomUUID(), resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test note text", response.getBody().noteText());
    }

    @Test
    @DisplayName("Should schedule test drive and return 201 Created")
    void shouldScheduleTestDrive() {
        ScheduleTestDriveResource resource = new ScheduleTestDriveResource(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), "Drive notes");
        when(commandService.handle(any(ScheduleTestDriveCommand.class))).thenReturn(Optional.of(sampleTestDrive));

        ResponseEntity<TestDriveResource> response = crmController.scheduleTestDrive(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should get my test drives and return 200 OK")
    void shouldGetMyTestDrives() {
        when(queryService.handle(any(GetTestDrivesForUserQuery.class))).thenReturn(List.of(sampleTestDrive));

        ResponseEntity<List<TestDriveResource>> response = crmController.getMyTestDrives();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should get test drive by ID")
    void shouldGetTestDriveById() {
        when(queryService.handle(any(GetTestDriveByIdQuery.class))).thenReturn(Optional.of(sampleTestDrive));

        ResponseEntity<TestDriveResource> response = crmController.getTestDriveById(UUID.randomUUID());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should update test drive status")
    void shouldUpdateTestDriveStatus() {
        UpdateTestDriveStatusResource resource = new UpdateTestDriveStatusResource("COMPLETED");
        when(commandService.handle(any(UpdateTestDriveStatusCommand.class))).thenReturn(Optional.of(sampleTestDrive));

        ResponseEntity<TestDriveResource> response = crmController.updateTestDriveStatus(UUID.randomUUID(), resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should cancel test drive and return 204 No Content")
    void shouldCancelTestDrive() {
        doNothing().when(commandService).handle(any(CancelTestDriveCommand.class));

        ResponseEntity<Void> response = crmController.cancelTestDrive(UUID.randomUUID());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commandService, times(1)).handle(any(CancelTestDriveCommand.class));
    }
}

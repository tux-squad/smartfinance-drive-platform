package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices.SubscriptionCommandService;
import com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices.SubscriptionQueryService;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.PayInvoiceCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetInvoiceByIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetInvoicesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.InvoiceResource;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoicesController REST Unit Tests")
class InvoicesControllerTest {

    @Mock
    private SubscriptionCommandService subscriptionCommandService;

    @Mock
    private SubscriptionQueryService subscriptionQueryService;

    @InjectMocks
    private InvoicesController controller;

    private Invoice sampleInvoice;

    @BeforeEach
    void setUp() {
        sampleInvoice = new Invoice(10L, "user-123", new BigDecimal("199.00"), "USD");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user-123", "password", Collections.emptyList()
        );
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails("user-123", "user-123"));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should get current user invoices and return 200 OK")
    void shouldGetCurrentUserInvoices() {
        when(subscriptionQueryService.handle(any(GetInvoicesByUserIdQuery.class))).thenReturn(List.of(sampleInvoice));

        ResponseEntity<List<InvoiceResource>> response = controller.getCurrentUserInvoices();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should get invoice PDF byte array")
    void shouldGetInvoicePdf() {
        when(subscriptionQueryService.handle(any(GetInvoiceByIdQuery.class))).thenReturn(Optional.of(sampleInvoice));

        ResponseEntity<byte[]> response = controller.getInvoicePdf(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    @DisplayName("Should pay invoice and return 200 OK")
    void shouldPayInvoice() {
        when(subscriptionCommandService.handle(any(PayInvoiceCommand.class))).thenReturn(Optional.of(sampleInvoice));

        ResponseEntity<InvoiceResource> response = controller.payInvoice(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}

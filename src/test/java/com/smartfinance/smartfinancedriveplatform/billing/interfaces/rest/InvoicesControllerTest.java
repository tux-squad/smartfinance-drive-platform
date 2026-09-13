package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices.SubscriptionCommandService;
import com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices.SubscriptionQueryService;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.PayInvoiceCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetInvoicesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.InvoiceStatus;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoicesController Unit Tests")
class InvoicesControllerTest {

    @Mock
    private SubscriptionCommandService subscriptionCommandService;

    @Mock
    private SubscriptionQueryService subscriptionQueryService;

    @InjectMocks
    private InvoicesController invoicesController;

    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        testInvoice = new Invoice(10L, "usr_100", new BigDecimal("99.99"), "USD");
        ReflectionTestUtils.setField(testInvoice, "id", 50L);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("usr_100", null, List.of());
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails("usr_100", "user@example.com"));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return invoices for current user on getCurrentUserInvoices")
    void shouldReturnCurrentUserInvoices() {
        when(subscriptionQueryService.handle(any(GetInvoicesByUserIdQuery.class))).thenReturn(List.of(testInvoice));

        ResponseEntity<List<InvoiceResource>> response = invoicesController.getCurrentUserInvoices();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(50L, response.getBody().get(0).id());
    }

    @Test
    @DisplayName("Should return 200 OK on successful payInvoice")
    void shouldPayInvoiceSuccessfully() {
        testInvoice.markPaid();
        when(subscriptionCommandService.handle(any(PayInvoiceCommand.class))).thenReturn(Optional.of(testInvoice));

        ResponseEntity<InvoiceResource> response = invoicesController.payInvoice(50L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(InvoiceStatus.PAID, response.getBody().status());
    }

    @Test
    @DisplayName("Should return 404 Not Found when paying non-existent invoice")
    void shouldReturnNotFoundWhenInvoiceDoesNotExist() {
        when(subscriptionCommandService.handle(any(PayInvoiceCommand.class))).thenReturn(Optional.empty());

        ResponseEntity<InvoiceResource> response = invoicesController.payInvoice(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}

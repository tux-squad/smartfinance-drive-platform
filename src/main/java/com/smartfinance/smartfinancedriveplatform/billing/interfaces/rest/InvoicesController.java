package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices.SubscriptionCommandService;
import com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices.SubscriptionQueryService;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.PayInvoiceCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetInvoicesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.InvoiceResource;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.transform.InvoiceResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User Invoice management.
 */
@RestController
@RequestMapping(value = "/api/v1/billing/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
public class InvoicesController {

    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;

    public InvoicesController(SubscriptionCommandService subscriptionCommandService,
                              SubscriptionQueryService subscriptionQueryService) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
    }

    /**
     * Retrieves all invoices issued to the current user.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InvoiceResource>> getCurrentUserInvoices() {
        String currentUserId = SecurityUtils.getRequiredCurrentUserId();
        var invoices = subscriptionQueryService.handle(new GetInvoicesByUserIdQuery(currentUserId));
        var resources = invoices.stream()
                .map(InvoiceResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Marks a pending invoice as paid directly in internal billing.
     * <p>
     * Note: This endpoint serves as an internal, manual, or offline payment reconciliation shortcut.
     * For automated credit card payments, use Stripe Checkout via {@code POST /api/v1/billing/subscriptions/checkout-session}
     * and handle completion via webhooks.
     * </p>
     */
    @PostMapping("/{invoiceId}/pay")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InvoiceResource> payInvoice(@PathVariable Long invoiceId) {
        String currentUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new PayInvoiceCommand(invoiceId, currentUserId);
        var invoiceOpt = subscriptionCommandService.handle(command);
        return invoiceOpt
                .map(invoice -> ResponseEntity.ok(InvoiceResourceFromEntityAssembler.toResourceFromEntity(invoice)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

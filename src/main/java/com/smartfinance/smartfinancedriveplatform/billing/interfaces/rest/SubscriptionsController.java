package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices.SubscriptionCommandService;
import com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices.SubscriptionQueryService;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.CancelSubscriptionCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.SubscribeUserCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetSubscriptionByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.SubscribeUserResource;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.SubscriptionResource;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.transform.SubscriptionResourceFromEntityAssembler;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.smartfinance.smartfinancedriveplatform.billing.application.outboundservices.StripePaymentGatewayService;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.CheckoutSessionResponseResource;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.CreateCheckoutSessionResource;

/**
 * REST Controller for User Subscription management.
 */
@RestController
@RequestMapping(value = "/api/v1/billing/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
public class SubscriptionsController {

    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;
    private final StripePaymentGatewayService stripePaymentGatewayService;

    public SubscriptionsController(SubscriptionCommandService subscriptionCommandService,
                                   SubscriptionQueryService subscriptionQueryService,
                                   StripePaymentGatewayService stripePaymentGatewayService) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
        this.stripePaymentGatewayService = stripePaymentGatewayService;
    }

    /**
     * Retrieves the current user's active subscription.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResource> getCurrentUserSubscription() {
        String currentUserId = SecurityUtils.getRequiredCurrentUserId();
        var subscriptionOpt = subscriptionQueryService.handle(new GetSubscriptionByUserIdQuery(currentUserId));
        return subscriptionOpt
                .map(sub -> ResponseEntity.ok(SubscriptionResourceFromEntityAssembler.toResourceFromEntity(sub)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Subscribes the current user to a plan.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResource> subscribeCurrentUser(@Valid @RequestBody SubscribeUserResource resource) {
        String currentUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new SubscribeUserCommand(currentUserId, resource.planId(), resource.autoRenew());
        var subscriptionOpt = subscriptionCommandService.handle(command);
        return subscriptionOpt
                .map(sub -> ResponseEntity.status(HttpStatus.CREATED).body(SubscriptionResourceFromEntityAssembler.toResourceFromEntity(sub)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Cancels an active subscription.
     */
    @DeleteMapping("/{subscriptionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResource> cancelSubscription(@PathVariable Long subscriptionId) {
        String currentUserId = SecurityUtils.getRequiredCurrentUserId();
        var command = new CancelSubscriptionCommand(subscriptionId, currentUserId);
        var subscriptionOpt = subscriptionCommandService.handle(command);
        return subscriptionOpt
                .map(sub -> ResponseEntity.ok(SubscriptionResourceFromEntityAssembler.toResourceFromEntity(sub)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Initiates a Stripe Checkout Session for subscription purchase.
     */
    @PostMapping("/checkout-session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckoutSessionResponseResource> createCheckoutSession(@Valid @RequestBody CreateCheckoutSessionResource resource) {
        String currentUserId = SecurityUtils.getRequiredCurrentUserId();
        String checkoutUrl = stripePaymentGatewayService.createCheckoutSession(
                null,
                resource.stripePriceId(),
                currentUserId,
                resource.successUrl(),
                resource.cancelUrl()
        );
        return ResponseEntity.ok(new CheckoutSessionResponseResource(checkoutUrl));
    }
}

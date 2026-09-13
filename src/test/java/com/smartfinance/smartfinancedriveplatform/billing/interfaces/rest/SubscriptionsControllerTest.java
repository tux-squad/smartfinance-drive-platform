package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices.SubscriptionCommandService;
import com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices.SubscriptionQueryService;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.SubscribeUserCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetSubscriptionByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.SubscribeUserResource;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.SubscriptionResource;
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

import com.smartfinance.smartfinancedriveplatform.billing.application.outboundservices.StripePaymentGatewayService;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.CheckoutSessionResponseResource;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.CreateCheckoutSessionResource;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.CancelSubscriptionCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionsController Unit Tests")
class SubscriptionsControllerTest {

    @Mock
    private SubscriptionCommandService subscriptionCommandService;

    @Mock
    private SubscriptionQueryService subscriptionQueryService;

    @Mock
    private StripePaymentGatewayService stripePaymentGatewayService;

    @InjectMocks
    private SubscriptionsController subscriptionsController;

    private Plan testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new Plan("DEALER_PRO", "Dealer Pro Plan", new BigDecimal("99.99"), "USD", BillingCycle.MONTHLY, 20, 100);
        ReflectionTestUtils.setField(testPlan, "id", 1L);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user@example.com", null, List.of());
        auth.setDetails(new SecurityUtils.AuthenticatedUserDetails("100", "user@example.com"));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return subscription on getCurrentUserSubscription when found")
    void shouldReturnCurrentUserSubscriptionWhenFound() {
        Subscription subscription = new Subscription("100", testPlan, true);
        ReflectionTestUtils.setField(subscription, "id", 10L);

        when(subscriptionQueryService.handle(any(GetSubscriptionByUserIdQuery.class))).thenReturn(Optional.of(subscription));

        ResponseEntity<SubscriptionResource> response = subscriptionsController.getCurrentUserSubscription();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("100", response.getBody().userId());
    }

    @Test
    @DisplayName("Should return 201 Created on subscribeCurrentUser")
    void shouldSubscribeCurrentUserSuccessfully() {
        Subscription subscription = new Subscription("100", testPlan, true);
        ReflectionTestUtils.setField(subscription, "id", 10L);

        when(subscriptionCommandService.handle(any(SubscribeUserCommand.class))).thenReturn(Optional.of(subscription));

        ResponseEntity<SubscriptionResource> response = subscriptionsController.subscribeCurrentUser(new SubscribeUserResource(1L, true));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("100", response.getBody().userId());
    }

    @Test
    @DisplayName("Should cancel subscription successfully and enforce ownership with current user ID")
    void shouldCancelSubscriptionSuccessfully() {
        Subscription subscription = new Subscription("100", testPlan, true);
        subscription.cancel();
        ReflectionTestUtils.setField(subscription, "id", 10L);

        when(subscriptionCommandService.handle(new CancelSubscriptionCommand(10L, "100")))
                .thenReturn(Optional.of(subscription));

        ResponseEntity<SubscriptionResource> response = subscriptionsController.cancelSubscription(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SubscriptionStatus.CANCELED, response.getBody().status());
        verify(subscriptionCommandService).handle(new CancelSubscriptionCommand(10L, "100"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when canceling non-existent or unauthorized subscription")
    void shouldReturnNotFoundWhenCancelingUnauthorizedSubscription() {
        when(subscriptionCommandService.handle(new CancelSubscriptionCommand(999L, "100")))
                .thenReturn(Optional.empty());

        ResponseEntity<SubscriptionResource> response = subscriptionsController.cancelSubscription(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(subscriptionCommandService).handle(new CancelSubscriptionCommand(999L, "100"));
    }

    @Test
    @DisplayName("Should return 200 OK with checkout URL and verify current user ID is passed")
    void shouldCreateCheckoutSessionSuccessfully() {
        when(stripePaymentGatewayService.createCheckoutSession(null, "price_123", "100", "http://success", "http://cancel"))
                .thenReturn("https://checkout.stripe.com/c/pay/cs_test_123");

        CreateCheckoutSessionResource resource = new CreateCheckoutSessionResource("price_123", "http://success", "http://cancel");
        ResponseEntity<CheckoutSessionResponseResource> response = subscriptionsController.createCheckoutSession(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("https://checkout.stripe.com/c/pay/cs_test_123", response.getBody().checkoutUrl());
        verify(stripePaymentGatewayService).createCheckoutSession(null, "price_123", "100", "http://success", "http://cancel");
    }
}

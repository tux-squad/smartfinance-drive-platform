package com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.CancelSubscriptionCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.SubscribeUserCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.InvoiceRepository;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.PlanRepository;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.SubscriptionRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.PayInvoiceCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.InvoiceStatus;
import java.util.List;
import static org.mockito.Mockito.verify;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionCommandServiceImpl Unit Tests")
class SubscriptionCommandServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private SubscriptionCommandServiceImpl subscriptionCommandService;

    private Plan testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new Plan("DEALER_PRO", "Dealer Pro Plan", new BigDecimal("99.99"), "USD", BillingCycle.MONTHLY, 20, 100);
        ReflectionTestUtils.setField(testPlan, "id", 1L);
    }

    @Test
    @DisplayName("Should create subscription and initial invoice on valid subscribeUser command")
    void shouldSubscribeUserSuccessfully() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription sub = inv.getArgument(0);
            ReflectionTestUtils.setField(sub, "id", 10L);
            return sub;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Subscription> result = subscriptionCommandService.handle(new SubscribeUserCommand("user-123", 1L, true));

        assertTrue(result.isPresent());
        assertEquals("user-123", result.get().getUserId());
        assertEquals(SubscriptionStatus.ACTIVE, result.get().getStatus());
    }

    @Test
    @DisplayName("Should cancel subscription successfully")
    void shouldCancelSubscriptionSuccessfully() {
        Subscription subscription = new Subscription("user-123", testPlan, true);
        ReflectionTestUtils.setField(subscription, "id", 10L);

        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Subscription> result = subscriptionCommandService.handle(new CancelSubscriptionCommand(10L, "user-123"));

        assertTrue(result.isPresent());
        assertEquals(SubscriptionStatus.CANCELED, result.get().getStatus());
        assertFalse(result.get().isAutoRenew());
    }

    @Test
    @DisplayName("Should throw exception when canceling someone else's subscription")
    void shouldThrowExceptionWhenCancelingUnauthorizedSubscription() {
        Subscription subscription = new Subscription("user-123", testPlan, true);
        ReflectionTestUtils.setField(subscription, "id", 10L);

        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));

        assertThrows(DomainValidationException.class, () ->
                subscriptionCommandService.handle(new CancelSubscriptionCommand(10L, "unauthorized-user")));
    }

    @Test
    @DisplayName("Should throw exception when paying an already paid invoice")
    void shouldThrowExceptionWhenPayingAlreadyPaidInvoice() {
        Invoice invoice = new Invoice(10L, "user-123", new BigDecimal("99.99"), "USD");
        invoice.markPaid();

        when(invoiceRepository.findById(100L)).thenReturn(Optional.of(invoice));

        assertThrows(DomainValidationException.class, () ->
                subscriptionCommandService.handle(new PayInvoiceCommand(100L, "user-123")));
    }

    @Test
    @DisplayName("Should handle Stripe checkout completed event successfully")
    void shouldHandleStripeCheckoutCompletedSuccessfully() {
        Subscription subscription = new Subscription("user-123", testPlan, true);
        Invoice pendingInvoice = new Invoice(10L, "user-123", new BigDecimal("99.99"), "USD");

        when(subscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc("user-123", SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(subscription));
        when(invoiceRepository.findAllByUserId("user-123")).thenReturn(List.of(pendingInvoice));

        subscriptionCommandService.handleStripeCheckoutCompleted("user-123", "cus_test_123", "sub_test_123");

        verify(subscriptionRepository).save(subscription);
        assertEquals("cus_test_123", subscription.getStripeCustomerId());
        assertEquals("sub_test_123", subscription.getStripeSubscriptionId());
        assertEquals(InvoiceStatus.PAID, pendingInvoice.getStatus());
    }

    @Test
    @DisplayName("Should handle Stripe subscription deleted event successfully")
    void shouldHandleStripeSubscriptionDeletedSuccessfully() {
        Subscription subscription = new Subscription("user-123", testPlan, true);
        subscription.updateStripeDetails("cus_test_123", "sub_test_123");

        when(subscriptionRepository.findByStripeSubscriptionId("sub_test_123"))
                .thenReturn(Optional.of(subscription));

        subscriptionCommandService.handleStripeSubscriptionDeleted("sub_test_123");

        verify(subscriptionRepository).save(subscription);
        assertEquals(SubscriptionStatus.CANCELED, subscription.getStatus());
    }

    @Test
    @DisplayName("Should handle Stripe payment failed event successfully")
    void shouldHandleStripePaymentFailedSuccessfully() {
        Subscription subscription = new Subscription("user-123", testPlan, true);
        subscription.updateStripeDetails("cus_test_123", "sub_test_123");

        when(subscriptionRepository.findByStripeCustomerId("cus_test_123"))
                .thenReturn(Optional.of(subscription));

        subscriptionCommandService.handleStripePaymentFailed("cus_test_123");

        verify(subscriptionRepository).save(subscription);
        assertEquals(SubscriptionStatus.PAST_DUE, subscription.getStatus());
    }
}

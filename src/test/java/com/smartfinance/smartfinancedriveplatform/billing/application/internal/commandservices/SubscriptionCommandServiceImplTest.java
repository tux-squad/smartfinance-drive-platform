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
        testPlan.setId(1L);
    }

    @Test
    @DisplayName("Should create subscription and initial invoice on valid subscribeUser command")
    void shouldSubscribeUserSuccessfully() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription sub = inv.getArgument(0);
            sub.setId(10L);
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
        subscription.setId(10L);

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
        subscription.setId(10L);

        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));

        assertThrows(DomainValidationException.class, () ->
                subscriptionCommandService.handle(new CancelSubscriptionCommand(10L, "unauthorized-user")));
    }
}

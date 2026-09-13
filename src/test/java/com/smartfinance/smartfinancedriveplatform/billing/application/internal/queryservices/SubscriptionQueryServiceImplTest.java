package com.smartfinance.smartfinancedriveplatform.billing.application.internal.queryservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetInvoicesByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.queries.GetSubscriptionByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.BillingCycle;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.InvoiceRepository;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionQueryServiceImpl Unit Tests")
class SubscriptionQueryServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private SubscriptionQueryServiceImpl subscriptionQueryService;

    private Subscription testSubscription;
    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        Plan plan = new Plan("DEALER_PRO", "Dealer Pro Tier", new BigDecimal("99.99"), "USD", BillingCycle.MONTHLY, 20, 100);
        testSubscription = new Subscription("usr_100", plan, true);
        ReflectionTestUtils.setField(testSubscription, "id", 10L);

        testInvoice = new Invoice(10L, "usr_100", new BigDecimal("99.99"), "USD");
        ReflectionTestUtils.setField(testInvoice, "id", 50L);
    }

    @Test
    @DisplayName("Should return active subscription for user on GetSubscriptionByUserIdQuery")
    void shouldReturnActiveSubscriptionForUser() {
        when(subscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc("usr_100", SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(testSubscription));

        Optional<Subscription> result = subscriptionQueryService.handle(new GetSubscriptionByUserIdQuery("usr_100"));

        assertTrue(result.isPresent());
        assertEquals("usr_100", result.get().getUserId());
        assertEquals(SubscriptionStatus.ACTIVE, result.get().getStatus());
    }

    @Test
    @DisplayName("Should return all user invoices on GetInvoicesByUserIdQuery")
    void shouldReturnAllUserInvoices() {
        when(invoiceRepository.findAllByUserId("usr_100")).thenReturn(List.of(testInvoice));

        List<Invoice> result = subscriptionQueryService.handle(new GetInvoicesByUserIdQuery("usr_100"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).getId());
    }
}

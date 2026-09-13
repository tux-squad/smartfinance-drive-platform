package com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.CancelSubscriptionCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.PayInvoiceCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.SubscribeUserCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.InvoiceRepository;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.PlanRepository;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.SubscriptionRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final InvoiceRepository invoiceRepository;

    public SubscriptionCommandServiceImpl(SubscriptionRepository subscriptionRepository,
                                           PlanRepository planRepository,
                                           InvoiceRepository invoiceRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(SubscribeUserCommand command) {
        Plan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new DomainValidationException("billing.error.planNotFound"));

        if (!plan.isActive()) {
            throw new DomainValidationException("billing.error.planNotActive");
        }

        // Cancel previous active subscription if present
        subscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(command.userId(), SubscriptionStatus.ACTIVE)
                .ifPresent(existingSub -> {
                    existingSub.cancel();
                    subscriptionRepository.save(existingSub);
                });

        Subscription newSubscription = new Subscription(command.userId(), plan, command.autoRenew());
        Subscription savedSubscription = subscriptionRepository.save(newSubscription);

        // Generate initial invoice
        Invoice invoice = new Invoice(
                savedSubscription.getId(),
                command.userId(),
                plan.getPrice(),
                plan.getCurrency()
        );
        invoiceRepository.save(invoice);

        return Optional.of(savedSubscription);
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(CancelSubscriptionCommand command) {
        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new DomainValidationException("billing.error.subscriptionNotFound"));

        if (!subscription.getUserId().equals(command.userId())) {
            throw new DomainValidationException("billing.error.unauthorizedSubscriptionAccess");
        }

        subscription.cancel();
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        return Optional.of(updatedSubscription);
    }

    @Override
    @Transactional
    public Optional<Invoice> handle(PayInvoiceCommand command) {
        Invoice invoice = invoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new DomainValidationException("billing.error.invoiceNotFound"));

        if (!invoice.getUserId().equals(command.userId())) {
            throw new DomainValidationException("billing.error.unauthorizedInvoiceAccess");
        }

        invoice.markPaid();
        Invoice paidInvoice = invoiceRepository.save(invoice);
        return Optional.of(paidInvoice);
    }
}

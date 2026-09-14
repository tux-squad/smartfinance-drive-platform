package com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.transform;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Subscription;
import com.smartfinance.smartfinancedriveplatform.billing.interfaces.rest.resources.SubscriptionResource;

public class SubscriptionResourceFromEntityAssembler {
    public static SubscriptionResource toResourceFromEntity(Subscription subscription) {
        if (subscription == null) return null;
        return new SubscriptionResource(
                subscription.getId(),
                subscription.getUserId(),
                PlanResourceFromEntityAssembler.toResourceFromEntity(subscription.getPlan()),
                subscription.getStatus(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.isAutoRenew(),
                subscription.isActive()
        );
    }
}

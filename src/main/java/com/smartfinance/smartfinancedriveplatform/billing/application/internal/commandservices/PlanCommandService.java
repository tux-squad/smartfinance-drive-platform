package com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.CreatePlanCommand;

import java.util.Optional;

/**
 * Application Command Service interface for creating SaaS plans.
 */
public interface PlanCommandService {
    Optional<Plan> handle(CreatePlanCommand command);
}

package com.smartfinance.smartfinancedriveplatform.billing.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Plan;
import com.smartfinance.smartfinancedriveplatform.billing.domain.model.commands.CreatePlanCommand;
import com.smartfinance.smartfinancedriveplatform.billing.domain.repositories.PlanRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PlanCommandServiceImpl implements PlanCommandService {

    private final PlanRepository planRepository;

    public PlanCommandServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional
    public Optional<Plan> handle(CreatePlanCommand command) {
        if (planRepository.findByName(command.name()).isPresent()) {
            throw new DomainValidationException("billing.error.planAlreadyExists");
        }

        Plan plan = new Plan(
                command.name(),
                command.description(),
                command.price(),
                command.currency(),
                command.billingCycle(),
                command.maxVehicleListings(),
                command.maxSimulationsPerMonth(),
                command.stripePriceId()
        );

        Plan savedPlan = planRepository.save(plan);
        return Optional.of(savedPlan);
    }
}

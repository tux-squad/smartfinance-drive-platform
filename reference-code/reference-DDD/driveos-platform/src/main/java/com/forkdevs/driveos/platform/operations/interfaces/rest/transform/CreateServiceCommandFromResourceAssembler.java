package com.forkdevs.driveos.platform.operations.interfaces.rest.transform;

import com.forkdevs.driveos.platform.operations.domain.model.commands.CreateServiceCommand;
import com.forkdevs.driveos.platform.operations.interfaces.rest.resources.CreateServiceResource;
import com.forkdevs.driveos.platform.shared.domain.model.valueobjects.BranchId;
import com.forkdevs.driveos.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;

public class CreateServiceCommandFromResourceAssembler {
    public static CreateServiceCommand toCommandFromResource(CreateServiceResource resource) {
        return new CreateServiceCommand(
                new BranchId(resource.branchId()),
                resource.name(),
                new Money(BigDecimal.valueOf(resource.price()))
        );
    }
}

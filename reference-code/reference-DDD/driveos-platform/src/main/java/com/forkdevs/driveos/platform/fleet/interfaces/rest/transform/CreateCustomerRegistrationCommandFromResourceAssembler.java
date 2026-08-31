package com.forkdevs.driveos.platform.fleet.interfaces.rest.transform;

import com.forkdevs.driveos.platform.fleet.domain.model.commands.CreateCustomerRegistrationCommand;
import com.forkdevs.driveos.platform.fleet.interfaces.rest.resources.CreateCustomerRegistrationResource;
import com.forkdevs.driveos.platform.shared.domain.model.valueobjects.BranchId;
import com.forkdevs.driveos.platform.shared.domain.model.valueobjects.CustomerId;

public class CreateCustomerRegistrationCommandFromResourceAssembler {

    public static CreateCustomerRegistrationCommand toCommandFromResource(CreateCustomerRegistrationResource resource) {
        return new CreateCustomerRegistrationCommand(
                new CustomerId(resource.customerId()),
                new BranchId(resource.branchId())
        );
    }
}


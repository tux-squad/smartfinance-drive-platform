package com.forkdevs.driveos.platform.operations.interfaces.rest.transform;

import com.forkdevs.driveos.platform.operations.domain.model.commands.UpdateServiceCommand;
import com.forkdevs.driveos.platform.operations.domain.model.valueobjects.ServiceId;
import com.forkdevs.driveos.platform.operations.interfaces.rest.resources.UpdateServiceResource;
import com.forkdevs.driveos.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdateServiceCommandFromResourceAssembler {
    public static UpdateServiceCommand toCommandFromResource(UUID ServiceId, UpdateServiceResource resource) {
        return new UpdateServiceCommand(
                new ServiceId(ServiceId),
                resource.name(),
                new Money(BigDecimal.valueOf(resource.price()))
        );
    }
}

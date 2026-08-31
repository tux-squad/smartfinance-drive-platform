package com.forkdevs.driveos.platform.iot.application.internal.queryservices;

import com.forkdevs.driveos.platform.core.application.queryservices.CustomerQueryService;
import com.forkdevs.driveos.platform.core.domain.model.queries.GetCustomerByIdQuery;
import com.forkdevs.driveos.platform.iot.application.queryservices.VehicleQueryService;
import com.forkdevs.driveos.platform.iot.domain.model.aggregates.Vehicle;
import com.forkdevs.driveos.platform.iot.domain.model.queries.GetActiveVehiclesByCustomerIdQuery;
import com.forkdevs.driveos.platform.iot.domain.model.queries.GetVehiclesAvailableForLinkingQuery;
import com.forkdevs.driveos.platform.iot.domain.model.queries.GetVehicleByIdQuery;
import com.forkdevs.driveos.platform.iot.domain.repositories.VehicleRegistrationRepository;
import com.forkdevs.driveos.platform.iot.domain.repositories.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for handling Vehicle queries inside the iot context.
 */
@Service
public class VehicleQueryServiceImpl implements VehicleQueryService {

    private final VehicleRepository vehicleRepository;
    private final CustomerQueryService customerQueryService;
    private final VehicleRegistrationRepository vehicleRegistrationRepository;

    public VehicleQueryServiceImpl(
            VehicleRepository vehicleRepository,
            CustomerQueryService customerQueryService,
            VehicleRegistrationRepository vehicleRegistrationRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.customerQueryService = customerQueryService;
        this.vehicleRegistrationRepository = vehicleRegistrationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> handle(GetVehiclesAvailableForLinkingQuery query) {
        return vehicleRepository.findAvailableForLinkingByBranchId(query.branchId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> handle(GetActiveVehiclesByCustomerIdQuery query) {
        var customerOpt = customerQueryService.handle(new GetCustomerByIdQuery(query.customerId()));
        if (customerOpt.isEmpty()) {
            return List.of();
        }
        var customer = customerOpt.get();
        var userId = customer.getUserId().value();
        var activeRegistrations = vehicleRegistrationRepository.findAllActiveByUserId(userId);
        return activeRegistrations.stream()
                .map(reg -> vehicleRepository.findById(reg.getVehicleId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicle> handle(GetVehicleByIdQuery query) {
        return vehicleRepository.findById(query.vehicleId());
    }
}

package com.smartfinance.smartfinancedriveplatform.partners.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.DealershipCommandService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateDealershipCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.UpdateDealershipCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of DealershipCommandService.
 */
@Service
public class DealershipCommandServiceImpl implements DealershipCommandService {

    private final DealershipRepository dealershipRepository;

    public DealershipCommandServiceImpl(DealershipRepository dealershipRepository) {
        this.dealershipRepository = dealershipRepository;
    }

    @Override
    @Transactional
    public Optional<Dealership> handle(CreateDealershipCommand command) {
        var existing = dealershipRepository.findByUserId(command.userId());
        if (existing.isPresent()) {
            // Update existing profile if already exists for this dealer user
            var dealership = existing.get();
            dealership.updateDetails(
                    command.ruc(),
                    command.name(),
                    command.address(),
                    command.phone(),
                    command.email(),
                    command.website(),
                    command.description(),
                    command.operatingHours()
            );
            return Optional.of(dealershipRepository.save(dealership));
        }

        Dealership dealership = new Dealership(
                command.userId(),
                command.ruc(),
                command.name(),
                command.address(),
                command.phone(),
                command.email(),
                command.website(),
                command.description(),
                command.operatingHours(),
                command.logoUrl(),
                command.bannerUrl()
        );
        return Optional.of(dealershipRepository.save(dealership));
    }

    @Override
    @Transactional
    public Optional<Dealership> handle(UpdateDealershipCommand command) {
        var existingOpt = dealershipRepository.findById(command.dealershipId());
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        var dealership = existingOpt.get();
        dealership.updateDetails(
                command.ruc(),
                command.name(),
                command.address(),
                command.phone(),
                command.email(),
                command.website(),
                command.description(),
                command.operatingHours()
        );
        return Optional.of(dealershipRepository.save(dealership));
    }

    @Override
    @Transactional
    public Optional<Dealership> updateLogo(DealershipId dealershipId, String logoUrl) {
        var existingOpt = dealershipRepository.findById(dealershipId);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        var dealership = existingOpt.get();
        dealership.setLogoUrl(logoUrl);
        return Optional.of(dealershipRepository.save(dealership));
    }

    @Override
    @Transactional
    public Optional<Dealership> updateBanner(DealershipId dealershipId, String bannerUrl) {
        var existingOpt = dealershipRepository.findById(dealershipId);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        var dealership = existingOpt.get();
        dealership.setBannerUrl(bannerUrl);
        return Optional.of(dealershipRepository.save(dealership));
    }
}

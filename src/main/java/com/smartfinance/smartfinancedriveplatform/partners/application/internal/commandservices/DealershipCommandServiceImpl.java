package com.smartfinance.smartfinancedriveplatform.partners.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.DealershipCommandService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateDealershipCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkDealershipToUserCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.UpdateDealershipCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
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
                    command.operatingHours(),
                    command.logoUrl(),
                    command.bannerUrl()
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
                command.operatingHours(),
                command.logoUrl(),
                command.bannerUrl()
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

    @Override
    @Transactional
    public Dealership handle(LinkDealershipToUserCommand command) {
        String userId = command.userId();
        String ruc = command.ruc();
        String name = command.name();
        String address = (command.address() != null && !command.address().isBlank()) 
                ? command.address() 
                : "Dirección fiscal registrada en SUNAT";
        String email = command.email();

        // 1. If user is already associated with a dealership, return it (update details if needed)
        var existingByUserId = dealershipRepository.findByUserId(userId);
        if (existingByUserId.isPresent()) {
            Dealership dealership = existingByUserId.get();
            boolean changed = false;
            if (dealership.getRuc() == null && ruc != null && !ruc.isBlank()) {
                dealership.setRuc(ruc);
                changed = true;
            }
            if ((dealership.getAddress() == null || dealership.getAddress().isBlank()) && address != null) {
                dealership.setAddress(address);
                changed = true;
            }
            if (changed) {
                return dealershipRepository.save(dealership);
            }
            return dealership;
        }

        // 2. Check if a dealership already exists with this RUC
        if (ruc != null && !ruc.isBlank()) {
            var existingByRuc = dealershipRepository.findByRuc(ruc);
            if (existingByRuc.isPresent()) {
                Dealership dealership = existingByRuc.get();
                if (dealership.getUserId() != null && !dealership.getUserId().equals(userId)) {
                    throw new DomainValidationException("partners.error.dealership.rucAlreadyLinked");
                }
                dealership.setUserId(userId);
                if (address != null && (dealership.getAddress() == null || dealership.getAddress().isBlank())) {
                    dealership.setAddress(address);
                }
                return dealershipRepository.save(dealership);
            }
        }

        // 3. Check if an unlinked dealership exists with this name
        if (name != null && !name.isBlank()) {
            var existingByName = dealershipRepository.findByName(name);
            if (existingByName.isPresent()) {
                Dealership dealership = existingByName.get();
                if (dealership.getUserId() == null) {
                    if (ruc != null && dealership.getRuc() != null && !dealership.getRuc().equals(ruc)) {
                        throw new DomainValidationException("partners.error.dealershipRucMismatch");
                    }
                    dealership.setUserId(userId);
                    if (dealership.getRuc() == null && ruc != null && !ruc.isBlank()) {
                        dealership.setRuc(ruc);
                    }
                    if (address != null && (dealership.getAddress() == null || dealership.getAddress().isBlank())) {
                        dealership.setAddress(address);
                    }
                    return dealershipRepository.save(dealership);
                } else if (!dealership.getUserId().equals(userId)) {
                    throw new DomainValidationException("partners.error.dealershipAlreadyExists");
                }
            }
        }

        // 4. Create new dealership with verified SUNAT details and corporate email
        Dealership newDealership = new Dealership(
                userId,
                ruc,
                name,
                address,
                null,
                email,
                null,
                "Concesionaria verificada mediante SUNAT y correo corporativo",
                "Lun-Vie 08:30 - 18:00",
                null,
                null
        );
        return dealershipRepository.save(newDealership);
    }
}

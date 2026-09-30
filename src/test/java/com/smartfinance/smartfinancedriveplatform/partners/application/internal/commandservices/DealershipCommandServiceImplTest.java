package com.smartfinance.smartfinancedriveplatform.partners.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkDealershipToUserCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.DealershipId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DealershipCommandServiceImpl Unit Tests")
class DealershipCommandServiceImplTest {

    @Mock
    private DealershipRepository dealershipRepository;

    private DealershipCommandServiceImpl dealershipCommandService;

    @BeforeEach
    void setUp() {
        dealershipCommandService = new DealershipCommandServiceImpl(dealershipRepository);
    }

    @Test
    @DisplayName("Should create new Dealership when user has no existing dealership")
    void shouldCreateNewDealershipWhenUserHasNoExistingDealership() {
        var command = new LinkDealershipToUserCommand(
                "user-100",
                "20349887714",
                "AUTOLAND S.A.",
                "AV. JAVIER PRADO ESTE 5020",
                "gerencia@autoland.com.pe"
        );

        when(dealershipRepository.findByUserId("user-100")).thenReturn(Optional.empty());
        when(dealershipRepository.findByRuc("20349887714")).thenReturn(Optional.empty());
        when(dealershipRepository.findByName("AUTOLAND S.A.")).thenReturn(Optional.empty());
        when(dealershipRepository.save(any(Dealership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dealership result = dealershipCommandService.handle(command);

        assertNotNull(result);
        assertEquals("user-100", result.getUserId());
        assertEquals("20349887714", result.getRuc());
        assertEquals("AUTOLAND S.A.", result.getName());
        assertEquals("AV. JAVIER PRADO ESTE 5020", result.getAddress());
        assertEquals("gerencia@autoland.com.pe", result.getEmail());
    }

    @Test
    @DisplayName("Should link existing unlinked Dealership by RUC to user")
    void shouldLinkExistingUnlinkedDealershipByRuc() {
        var command = new LinkDealershipToUserCommand(
                "user-100",
                "20349887714",
                "AUTOLAND S.A.",
                "AV. JAVIER PRADO ESTE 5020",
                "gerencia@autoland.com.pe"
        );

        var existingUnlinked = new Dealership(
                new DealershipId(UUID.randomUUID()),
                null,
                "20349887714",
                "AUTOLAND S.A.",
                "AV. JAVIER PRADO ESTE 5020",
                null, null, null, null, null, 5.0, null, null, true
        );

        when(dealershipRepository.findByUserId("user-100")).thenReturn(Optional.empty());
        when(dealershipRepository.findByRuc("20349887714")).thenReturn(Optional.of(existingUnlinked));
        when(dealershipRepository.save(any(Dealership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dealership result = dealershipCommandService.handle(command);

        assertEquals("user-100", result.getUserId());
        assertEquals("20349887714", result.getRuc());
    }

    @Test
    @DisplayName("Should reject linking if RUC is already linked to another user")
    void shouldRejectIfRucAlreadyLinkedToAnotherUser() {
        var command = new LinkDealershipToUserCommand(
                "user-attacker",
                "20349887714",
                "AUTOLAND S.A.",
                "AV. JAVIER PRADO ESTE 5020",
                "attacker@autoland.com.pe"
        );

        var existingOwned = new Dealership(
                new DealershipId(UUID.randomUUID()),
                "legitimate-owner",
                "20349887714",
                "AUTOLAND S.A.",
                "AV. JAVIER PRADO ESTE 5020",
                null, null, null, null, null, 5.0, null, null, true
        );

        when(dealershipRepository.findByUserId("user-attacker")).thenReturn(Optional.empty());
        when(dealershipRepository.findByRuc("20349887714")).thenReturn(Optional.of(existingOwned));

        assertThrows(DomainValidationException.class, () -> dealershipCommandService.handle(command));
    }
}

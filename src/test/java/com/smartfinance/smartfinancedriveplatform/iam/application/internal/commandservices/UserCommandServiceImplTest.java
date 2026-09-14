package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.User;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestDealerRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.RequestFinancialInstitutionRoleCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Password;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Roles;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.Username;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.UserRepository;
import com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.SunatRucVerifierService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.SunatRucInfo;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCommandServiceImpl Role Verification Unit Tests")
class UserCommandServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SunatRucVerifierService sunatRucVerifierService;

    @InjectMocks
    private UserCommandServiceImpl userCommandService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, new Username("user@example.com"), new Password("$2a$12$hashedPassword123"), new ArrayList<>(List.of(Roles.ROLE_USER)));
    }

    @Test
    @DisplayName("Should assign ROLE_DEALER when RUC is active, habido and has automotive CIIU")
    void shouldAssignDealerRoleWhenSunatVerificationPasses() {
        SunatRucInfo validDealerInfo = new SunatRucInfo("20100128056", "TOYOTA PERU", "ACTIVO", "HABIDO", "SA", "150101", "DIR", "4510");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sunatRucVerifierService.verifyRuc("20100128056")).thenReturn(Optional.of(validDealerInfo));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = userCommandService.handle(new RequestDealerRoleCommand(1L, "20100128056"));

        assertTrue(result.isPresent());
        assertTrue(result.get().getRoles().contains(Roles.ROLE_DEALER));
    }

    @Test
    @DisplayName("Should throw exception when requesting dealer role with non-automotive CIIU")
    void shouldThrowExceptionWhenDealerRucIsNotAutomotive() {
        SunatRucInfo nonDealerInfo = new SunatRucInfo("20100047218", "BANCO X", "ACTIVO", "HABIDO", "SA", "150101", "DIR", "6419");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sunatRucVerifierService.verifyRuc("20100047218")).thenReturn(Optional.of(nonDealerInfo));

        assertThrows(DomainValidationException.class, () ->
                userCommandService.handle(new RequestDealerRoleCommand(1L, "20100047218")));
    }

    @Test
    @DisplayName("Should assign ROLE_FINANCIAL_INSTITUTION when RUC is active, habido and has financial CIIU")
    void shouldAssignFinancialInstitutionRoleWhenSunatVerificationPasses() {
        SunatRucInfo validFinanceInfo = new SunatRucInfo("20100047218", "BBVA PERU", "ACTIVO", "HABIDO", "SA", "150131", "DIR", "6419");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sunatRucVerifierService.verifyRuc("20100047218")).thenReturn(Optional.of(validFinanceInfo));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = userCommandService.handle(new RequestFinancialInstitutionRoleCommand(1L, "20100047218"));

        assertTrue(result.isPresent());
        assertTrue(result.get().getRoles().contains(Roles.ROLE_FINANCIAL_INSTITUTION));
    }

    @Test
    @DisplayName("Should throw exception when requesting financial institution role with non-financial CIIU")
    void shouldThrowExceptionWhenFinancialRucIsNotFinancialCiiu() {
        SunatRucInfo dealerInfo = new SunatRucInfo("20100128056", "TOYOTA PERU", "ACTIVO", "HABIDO", "SA", "150101", "DIR", "4510");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sunatRucVerifierService.verifyRuc("20100128056")).thenReturn(Optional.of(dealerInfo));

        assertThrows(DomainValidationException.class, () ->
                userCommandService.handle(new RequestFinancialInstitutionRoleCommand(1L, "20100128056")));
    }

    @Test
    @DisplayName("Should return null safely when handling ForgotPasswordCommand for non-existing username")
    void shouldReturnNullSafelyWhenUserNotFoundInForgotPassword() {
        when(userRepository.findByUsername(any(Username.class))).thenReturn(Optional.empty());

        String resetToken = userCommandService.handle(new com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ForgotPasswordCommand(new Username("nonexistent@example.com")));

        assertNull(resetToken);
    }
}

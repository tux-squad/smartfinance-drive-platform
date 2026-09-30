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
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.CorporateVerificationSession;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCommandServiceImpl Role Verification Unit Tests")
class UserCommandServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SunatRucVerifierService sunatRucVerifierService;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.FinancialEntityCommandService financialEntityCommandService;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.DealershipCommandService dealershipCommandService;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.partners.domain.services.CorporateDomainCatalog corporateDomainCatalog;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.CorporateVerificationSessionRepository corporateVerificationSessionRepository;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.OtpGeneratorService otpGeneratorService;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.EmailSenderService emailSenderService;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository financialEntityRepository;

    @Mock
    private com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.DealershipRepository dealershipRepository;

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
        verify(financialEntityCommandService, times(1)).handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkFinancialEntityToUserCommand.class));
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

    @Test
    @DisplayName("Should successfully initiate corporate verification for Bank")
    void shouldSuccessfullyInitiateCorporateVerificationForBank() {
        String ruc = "20100047218";
        String email = "funcionario@viabcp.com";
        var rucInfo = new SunatRucInfo(ruc, "BANCO DE CREDITO DEL PERU", "ACTIVO", "HABIDO", "SAA", "150101", "CALLE 1", "6419");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sunatRucVerifierService.verifyRuc(ruc)).thenReturn(Optional.of(rucInfo));
        when(corporateDomainCatalog.determineEntityType(rucInfo)).thenReturn(com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.CorporateEntityType.FINANCIAL_INSTITUTION);
        when(corporateDomainCatalog.isDomainAllowedForRuc(eq(ruc), eq(email), any())).thenReturn(true);
        when(otpGeneratorService.generateOtp()).thenReturn("123456");
        when(otpGeneratorService.hashOtp("123456")).thenReturn("hashed123456");
        when(corporateVerificationSessionRepository.save(any(CorporateVerificationSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = userCommandService.handle(new com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.InitiateCorporateVerificationCommand("1", ruc, email));

        assertNotNull(result);
        assertTrue(result.sessionActive());
        assertEquals(600, result.expiresInSeconds());
        verify(emailSenderService, times(1)).sendCorporateVerificationOtp(eq(email), any(), eq("BANCO DE CREDITO DEL PERU"), eq("123456"), eq(10));
    }

    @Test
    @DisplayName("Should reject initiate corporate verification when email domain is not authorized")
    void shouldRejectInitiateWhenDomainNotAuthorized() {
        String ruc = "20100047218";
        String email = "impostor@gmail.com";
        var rucInfo = new SunatRucInfo(ruc, "BANCO DE CREDITO DEL PERU", "ACTIVO", "HABIDO", "SAA", "150101", "CALLE 1", "6419");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(sunatRucVerifierService.verifyRuc(ruc)).thenReturn(Optional.of(rucInfo));
        when(corporateDomainCatalog.determineEntityType(rucInfo)).thenReturn(com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.CorporateEntityType.FINANCIAL_INSTITUTION);
        when(corporateDomainCatalog.isDomainAllowedForRuc(eq(ruc), eq(email), any())).thenReturn(false);

        assertThrows(DomainValidationException.class, () ->
                userCommandService.handle(new com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.InitiateCorporateVerificationCommand("1", ruc, email)));
    }

    @Test
    @DisplayName("Should reject initiate corporate verification when user exceeds daily limit of 5 requests")
    void shouldRejectInitiateWhenDailyLimitExceeded() {
        String ruc = "20100047218";
        String email = "funcionario@viabcp.com";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(corporateVerificationSessionRepository.countRecentSessionsByUserId(eq("1"), any())).thenReturn(5L);

        var ex = assertThrows(DomainValidationException.class, () ->
                userCommandService.handle(new com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.InitiateCorporateVerificationCommand("1", ruc, email)));

        assertEquals("iam.error.corporateVerification.dailyLimitExceeded", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject initiate corporate verification when 60-second cooldown is still active")
    void shouldRejectInitiateWhenCooldownIsActive() {
        String ruc = "20100047218";
        String email = "funcionario@viabcp.com";
        // Session created 30 seconds ago (expires in 9m 30s)
        var recentSession = new CorporateVerificationSession(
                "1", ruc, email, "hash1", "FINANCIAL_INSTITUTION", "ROLE_FINANCIAL_INSTITUTION",
                "BANCO DE CREDITO", "DIR", java.time.Instant.now().plus(9, java.time.temporal.ChronoUnit.MINUTES).plus(30, java.time.temporal.ChronoUnit.SECONDS)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(corporateVerificationSessionRepository.countRecentSessionsByUserId(eq("1"), any())).thenReturn(1L);
        when(corporateVerificationSessionRepository.findLatestActiveSession("1", ruc)).thenReturn(Optional.of(recentSession));

        var ex = assertThrows(DomainValidationException.class, () ->
                userCommandService.handle(new com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.InitiateCorporateVerificationCommand("1", ruc, email)));

        assertEquals("iam.error.corporateVerification.cooldownActive", ex.getMessage());
    }

    @Test
    @DisplayName("Should successfully confirm corporate verification and auto-profile Dealership")
    void shouldSuccessfullyConfirmCorporateVerificationAndAutoProfileDealership() {
        String ruc = "20349887714";
        String code = "654321";
        var session = new CorporateVerificationSession(
                "1", ruc, "gerencia@autoland.com.pe", "hash654321",
                "DEALERSHIP", "ROLE_DEALER", "AUTOLAND S.A.", "AV. JAVIER PRADO 5020",
                java.time.Instant.now().plus(10, java.time.temporal.ChronoUnit.MINUTES)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(corporateVerificationSessionRepository.findLatestActiveSession("1", ruc)).thenReturn(Optional.of(session));
        when(otpGeneratorService.verifyOtp(code, "hash654321")).thenReturn(true);
        when(dealershipCommandService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkDealershipToUserCommand.class)))
                .thenReturn(new com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership("1", ruc, "AUTOLAND S.A.", "AV. JAVIER PRADO 5020", null, "gerencia@autoland.com.pe", null, null, null, null, null));

        var result = userCommandService.handle(new com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ConfirmCorporateVerificationCommand("1", ruc, code));

        assertNotNull(result);
        assertTrue(result.verified());
        assertEquals("DEALERSHIP", result.entityType());
        assertEquals("ROLE_DEALER", result.assignedRole());
        assertEquals("AUTOLAND S.A.", result.profileName());
        assertTrue(testUser.getRoles().contains(Roles.ROLE_DEALER));
        verify(dealershipCommandService, times(1)).handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkDealershipToUserCommand.class));
    }

    @Test
    @DisplayName("Should reject confirmation when OTP code does not match")
    void shouldRejectConfirmationWhenOtpInvalid() {
        String ruc = "20349887714";
        String invalidCode = "000000";
        var session = new CorporateVerificationSession(
                "1", ruc, "gerencia@autoland.com.pe", "hash654321",
                "DEALERSHIP", "ROLE_DEALER", "AUTOLAND S.A.", "AV. JAVIER PRADO 5020",
                java.time.Instant.now().plus(10, java.time.temporal.ChronoUnit.MINUTES)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(corporateVerificationSessionRepository.findLatestActiveSession("1", ruc)).thenReturn(Optional.of(session));
        when(otpGeneratorService.verifyOtp(invalidCode, "hash654321")).thenReturn(false);

        assertThrows(DomainValidationException.class, () ->
                userCommandService.handle(new com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.ConfirmCorporateVerificationCommand("1", ruc, invalidCode)));

        assertEquals(1, session.getAttempts());
        verify(corporateVerificationSessionRepository, times(1)).save(session);
    }
}

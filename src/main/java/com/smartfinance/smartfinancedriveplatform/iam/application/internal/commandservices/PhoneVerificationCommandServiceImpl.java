package com.smartfinance.smartfinancedriveplatform.iam.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.FirebaseTokenVerifierService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates.PhoneVerificationSession;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.commands.VerifyFirebasePhoneTokenCommand;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.FirebasePhoneClaims;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneNumber;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationResult;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.iam.domain.repositories.PhoneVerificationSessionRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.ExternalServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for handling phone verification workflows using Firebase Phone Authentication.
 */
@Service
public class PhoneVerificationCommandServiceImpl implements PhoneVerificationCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhoneVerificationCommandServiceImpl.class);

    private final PhoneVerificationSessionRepository sessionRepository;
    private final FirebaseTokenVerifierService firebaseTokenVerifierService;

    @Autowired
    public PhoneVerificationCommandServiceImpl(
            PhoneVerificationSessionRepository sessionRepository,
            @Autowired(required = false) FirebaseTokenVerifierService firebaseTokenVerifierService) {
        this.sessionRepository = sessionRepository;
        this.firebaseTokenVerifierService = firebaseTokenVerifierService;
    }

    @Override
    @Transactional
    public PhoneVerificationResult handle(VerifyFirebasePhoneTokenCommand command) {
        if (command == null || command.firebaseIdToken() == null || command.firebaseIdToken().isBlank()) {
            throw new DomainValidationException("iam.error.firebaseToken.required");
        }

        if (firebaseTokenVerifierService == null) {
            LOGGER.error("FirebaseTokenVerifierService is not configured or available in application context");
            throw new ExternalServiceUnavailableException(
                    "iam.error.phoneVerification.serviceUnavailable",
                    "El servicio de verificación de Firebase no está disponible.",
                    null
            );
        }

        FirebasePhoneClaims claims = firebaseTokenVerifierService.verifyToken(command.firebaseIdToken());
        PhoneNumber phone = new PhoneNumber(claims.phoneNumber());

        // 1. Invalidate prior pending sessions for this number
        sessionRepository.expirePendingSessions(phone.fullNumber());

        // 2. Create and persist verified session from Firebase Phone Auth
        PhoneVerificationSession verifiedSession = PhoneVerificationSession.createVerifiedFromFirebase(
                command.callerUserId(),
                phone
        );
        PhoneVerificationSession savedSession = sessionRepository.save(verifiedSession);

        LOGGER.info("Phone number [{}] successfully verified via Firebase Phone Auth [session: {}, uid: {}]",
                phone.getMasked(), savedSession.getId(), claims.uid());

        return new PhoneVerificationResult(
                true,
                phone.fullNumber(),
                PhoneVerificationStatus.VERIFIED,
                savedSession.getVerifiedAt(),
                savedSession.getVerificationToken(),
                "Número de teléfono verificado exitosamente mediante Firebase"
        );
    }
}

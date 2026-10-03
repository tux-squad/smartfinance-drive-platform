package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.FirebaseTokenVerifierService;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.FirebasePhoneClaims;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.ExternalServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Adapter implementation for verifying Firebase ID Tokens using Google Firebase Admin SDK.
 */
@Service
public class FirebaseTokenVerifierServiceImpl implements FirebaseTokenVerifierService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenVerifierServiceImpl.class);

    private final FirebaseAuth firebaseAuth;

    @Autowired
    public FirebaseTokenVerifierServiceImpl(@Autowired(required = false) FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public FirebasePhoneClaims verifyToken(String firebaseIdToken) {
        if (firebaseIdToken == null || firebaseIdToken.isBlank()) {
            throw new DomainValidationException("iam.error.firebaseToken.required");
        }

        if (firebaseAuth == null) {
            log.error("FirebaseAuth is not initialized. Please configure FIREBASE_PROJECT_ID or Firebase credentials");
            throw new ExternalServiceUnavailableException(
                    "iam.error.phoneVerification.serviceUnavailable",
                    "El servicio de verificación de Firebase no está configurado en el servidor.",
                    null
            );
        }

        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(firebaseIdToken.trim());
            String uid = decodedToken.getUid();

            // Extract phone_number claim
            String phoneNumber = (String) decodedToken.getClaims().get("phone_number");
            if (phoneNumber == null || phoneNumber.isBlank()) {
                log.warn("Firebase ID token for UID [{}] does not contain a verified 'phone_number' claim", uid);
                throw new DomainValidationException("iam.error.phoneVerification.noPhoneNumberInToken");
            }

            log.info("Successfully verified Firebase ID token for phone [{}] (UID: {})", maskPhone(phoneNumber), uid);
            return new FirebasePhoneClaims(uid, phoneNumber, true);
        } catch (FirebaseAuthException e) {
            log.warn("Firebase ID token verification failed: code={}, message={}", e.getAuthErrorCode(), e.getMessage());
            throw new DomainValidationException("iam.error.firebaseToken.invalid");
        } catch (DomainValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error verifying Firebase ID token: {}", e.getMessage(), e);
            throw new ExternalServiceUnavailableException(
                    "iam.error.phoneVerification.serviceUnavailable",
                    "Error al comunicarse con los servidores de autenticación de Google Firebase.",
                    e
            );
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 5) + "****" + phone.substring(phone.length() - 2);
    }
}

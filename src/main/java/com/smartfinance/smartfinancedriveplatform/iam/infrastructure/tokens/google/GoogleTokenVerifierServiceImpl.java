package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens.GoogleTokenVerifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * Service implementation for Google ID Token verification using official Google API Client library.
 */
@Service
public class GoogleTokenVerifierServiceImpl implements GoogleTokenVerifierService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifierServiceImpl.class);

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierServiceImpl(@Value("${google.client-id}") String clientId) {
        this(new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .build());
    }

    public GoogleTokenVerifierServiceImpl(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public Optional<GoogleUserInfo> verifyToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return Optional.empty();
        }

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                log.warn("Invalid Google ID token supplied");
                return Optional.empty();
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String givenName = (String) payload.get("given_name");
            String familyName = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture");
            Boolean emailVerified = payload.getEmailVerified();

            if (email == null || email.isBlank()) {
                log.warn("Google ID token payload did not contain email");
                return Optional.empty();
            }

            return Optional.of(new GoogleUserInfo(email, givenName, familyName, pictureUrl, Boolean.TRUE.equals(emailVerified)));
        } catch (Exception e) {
            log.error("Failed to verify Google ID token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}

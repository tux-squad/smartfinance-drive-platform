package com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.tokens;

import java.util.Optional;

/**
 * Outbound service port for verifying Google ID Tokens.
 */
public interface GoogleTokenVerifierService {

    /**
     * Value object containing verified Google user information.
     */
    record GoogleUserInfo(String email, String givenName, String familyName, String pictureUrl) {}

    /**
     * Verifies a Google ID token string and returns GoogleUserInfo if valid.
     *
     * @param idToken The raw ID token string from Google.
     * @return Optional containing GoogleUserInfo if valid, empty otherwise.
     */
    Optional<GoogleUserInfo> verifyToken(String idToken);
}

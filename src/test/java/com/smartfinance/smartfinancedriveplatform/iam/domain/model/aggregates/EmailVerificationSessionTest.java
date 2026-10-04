package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.EmailVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailVerificationSession Aggregate Unit Tests")
class EmailVerificationSessionTest {

    @Test
    @DisplayName("Should create active session in PENDING state")
    void shouldCreateActiveSessionInPendingState() {
        EmailVerificationSession session = new EmailVerificationSession("test@example.com", "hash123");

        assertNotNull(session.getId());
        assertEquals("test@example.com", session.getEmail());
        assertEquals("hash123", session.getCodeHash());
        assertEquals(0, session.getAttempts());
        assertEquals(EmailVerificationStatus.PENDING, session.getStatus());
        assertNull(session.getVerificationToken());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getExpiresAt());
        assertNull(session.getVerifiedAt());
        assertFalse(session.isExpired());
    }

    @Test
    @DisplayName("Should throw exception if email is null or blank on creation")
    void shouldThrowExceptionIfEmailIsBlank() {
        assertThrows(DomainValidationException.class, () -> new EmailVerificationSession("", "hash"));
        assertThrows(DomainValidationException.class, () -> new EmailVerificationSession(null, "hash"));
    }

    @Test
    @DisplayName("Should throw exception if codeHash is null or blank on creation")
    void shouldThrowExceptionIfCodeHashIsBlank() {
        assertThrows(DomainValidationException.class, () -> new EmailVerificationSession("test@example.com", ""));
        assertThrows(DomainValidationException.class, () -> new EmailVerificationSession("test@example.com", null));
    }

    @Test
    @DisplayName("Should mark session as VERIFIED when hash matches")
    void shouldMarkSessionAsVerifiedWhenHashMatches() {
        EmailVerificationSession session = new EmailVerificationSession("test@example.com", "hash123");

        session.verify(true);

        assertEquals(EmailVerificationStatus.VERIFIED, session.getStatus());
        assertEquals(1, session.getAttempts());
        assertNotNull(session.getVerifiedAt());
        assertNotNull(session.getVerificationToken());
    }

    @Test
    @DisplayName("Should increment attempts and throw invalidCode when hash does not match")
    void shouldIncrementAttemptsAndThrowWhenHashDoesNotMatch() {
        EmailVerificationSession session = new EmailVerificationSession("test@example.com", "hash123");

        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> session.verify(false));
        assertEquals("iam.error.emailVerification.invalidCode", ex.getMessage());
        assertEquals(1, session.getAttempts());
        assertEquals(EmailVerificationStatus.PENDING, session.getStatus());
    }

    @Test
    @DisplayName("Should block session when reaching 3 failed attempts")
    void shouldBlockSessionWhenReachingThreeFailedAttempts() {
        EmailVerificationSession session = new EmailVerificationSession("test@example.com", "hash123");

        assertThrows(DomainValidationException.class, () -> session.verify(false));
        assertThrows(DomainValidationException.class, () -> session.verify(false));

        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> session.verify(false));
        assertEquals("iam.error.emailVerification.maxAttemptsExceeded", ex.getMessage());
        assertEquals(3, session.getAttempts());
        assertEquals(EmailVerificationStatus.BLOCKED, session.getStatus());

        // Subsequent verification attempts on blocked session
        DomainValidationException blockedEx = assertThrows(DomainValidationException.class, () -> session.verify(true));
        assertEquals("iam.error.emailVerification.sessionBlocked", blockedEx.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when attempting to verify already verified session")
    void shouldThrowWhenVerifyingAlreadyVerifiedSession() {
        EmailVerificationSession session = new EmailVerificationSession("test@example.com", "hash123");
        session.verify(true);

        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> session.verify(true));
        assertEquals("iam.error.emailVerification.alreadyVerified", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when session is expired")
    void shouldThrowWhenSessionIsExpired() {
        Instant past = Instant.now().minus(20, ChronoUnit.MINUTES);
        EmailVerificationSession session = new EmailVerificationSession(
                java.util.UUID.randomUUID(),
                "test@example.com",
                "hash123",
                0,
                EmailVerificationStatus.PENDING,
                null,
                past,
                past.plus(10, ChronoUnit.MINUTES),
                null
        );

        assertTrue(session.isExpired());
        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> session.verify(true));
        assertEquals("iam.error.emailVerification.sessionExpired", ex.getMessage());
        assertEquals(EmailVerificationStatus.EXPIRED, session.getStatus());
    }
}

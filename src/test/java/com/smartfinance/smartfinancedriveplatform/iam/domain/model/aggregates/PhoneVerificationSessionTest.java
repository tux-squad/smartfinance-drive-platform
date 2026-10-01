package com.smartfinance.smartfinancedriveplatform.iam.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneNumber;
import com.smartfinance.smartfinancedriveplatform.iam.domain.model.valueobjects.PhoneVerificationStatus;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PhoneVerificationSession Aggregate Unit Tests")
class PhoneVerificationSessionTest {

    @Test
    @DisplayName("Should create active session in PENDING state")
    void shouldCreateActiveSessionInPendingState() {
        PhoneNumber phone = new PhoneNumber("+51 993913924");
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(5));

        PhoneVerificationSession session = new PhoneVerificationSession(phone, "dummy-sha256-hash", now, expiresAt);

        assertNotNull(session.getId());
        assertEquals("51993913924", session.getPhoneNumber().fullNumber());
        assertEquals("dummy-sha256-hash", session.getCodeHash());
        assertEquals(0, session.getAttempts());
        assertEquals(PhoneVerificationStatus.PENDING, session.getStatus());
        assertTrue(session.isPending());
        assertFalse(session.isBlocked());
        assertFalse(session.isExpired());
        assertNull(session.getVerifiedAt());
    }

    @Test
    @DisplayName("Should block session after 3 failed attempts")
    void shouldBlockSessionAfterThreeFailedAttempts() {
        PhoneNumber phone = new PhoneNumber("51993913924");
        PhoneVerificationSession session = new PhoneVerificationSession(phone, "hash", Instant.now(), Instant.now().plus(Duration.ofMinutes(5)));

        session.recordFailedAttempt();
        assertEquals(1, session.getAttempts());
        assertTrue(session.isPending());

        session.recordFailedAttempt();
        assertEquals(2, session.getAttempts());
        assertTrue(session.isPending());

        session.recordFailedAttempt();
        assertEquals(3, session.getAttempts());
        assertEquals(PhoneVerificationStatus.BLOCKED, session.getStatus());
        assertTrue(session.isBlocked());
        assertFalse(session.isPending());
    }

    @Test
    @DisplayName("Should mark session as VERIFIED when valid")
    void shouldMarkSessionAsVerified() {
        PhoneNumber phone = new PhoneNumber("51993913924");
        PhoneVerificationSession session = new PhoneVerificationSession(phone, "hash", Instant.now(), Instant.now().plus(Duration.ofMinutes(5)));

        session.markVerified();

        assertEquals(PhoneVerificationStatus.VERIFIED, session.getStatus());
        assertNotNull(session.getVerifiedAt());
        assertFalse(session.isPending());
    }

    @Test
    @DisplayName("Should detect expired session and transition status on isPending")
    void shouldDetectExpiredSession() {
        PhoneNumber phone = new PhoneNumber("51993913924");
        Instant past = Instant.now().minus(Duration.ofMinutes(10));
        PhoneVerificationSession session = new PhoneVerificationSession(phone, "hash", past, past.plus(Duration.ofMinutes(5)));

        assertTrue(session.isExpired());
        assertFalse(session.isPending());
        assertEquals(PhoneVerificationStatus.EXPIRED, session.getStatus());
    }

    @Test
    @DisplayName("Should check resend cooldown properly")
    void shouldCheckResendCooldownProperly() {
        PhoneNumber phone = new PhoneNumber("51993913924");
        Instant now = Instant.now();
        PhoneVerificationSession session = new PhoneVerificationSession(phone, "hash", now, now.plus(Duration.ofMinutes(5)));

        assertFalse(session.canResend(now.plusSeconds(30)));
        assertTrue(session.canResend(now.plusSeconds(61)));
    }

    @Test
    @DisplayName("Should throw exception when creating session with null phone or empty hash")
    void shouldThrowExceptionForInvalidInputs() {
        assertThrows(DomainValidationException.class, () -> new PhoneVerificationSession(null, "hash", Instant.now(), Instant.now().plus(Duration.ofMinutes(5))));
        assertThrows(DomainValidationException.class, () -> new PhoneVerificationSession(new PhoneNumber("51993913924"), "", Instant.now(), Instant.now().plus(Duration.ofMinutes(5))));
    }
}

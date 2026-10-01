package com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.tokens.reniec;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ReniecDniInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FactilizaDniVerifierServiceImpl Unit Tests")
class FactilizaDniVerifierServiceImplTest {

    @Test
    @DisplayName("Should return empty Optional when invalid DNI length, non-numeric, or null format supplied")
    void shouldReturnEmptyWhenInvalidDniFormat() {
        FactilizaDniVerifierServiceImpl service = new FactilizaDniVerifierServiceImpl("https://api.factiliza.com", "dummy-key");

        Optional<ReniecDniInfo> resultShort = service.verifyDni("1234");
        Optional<ReniecDniInfo> resultLong = service.verifyDni("123456789");
        Optional<ReniecDniInfo> resultAlpha = service.verifyDni("ABCDEFGH");
        Optional<ReniecDniInfo> resultNull = service.verifyDni(null);

        assertTrue(resultShort.isEmpty());
        assertTrue(resultLong.isEmpty());
        assertTrue(resultAlpha.isEmpty());
        assertTrue(resultNull.isEmpty());
    }
}

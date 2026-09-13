package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.tokens.sunat;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.SunatRucInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChequeaRucVerifierServiceImpl Unit Tests")
class ChequeaRucVerifierServiceImplTest {

    @Test
    @DisplayName("Should return empty Optional when invalid RUC length or null format supplied")
    void shouldReturnEmptyWhenInvalidRucFormat() {
        ChequeaRucVerifierServiceImpl service = new ChequeaRucVerifierServiceImpl("https://api.chequea.pe", "dummy-key");

        Optional<SunatRucInfo> resultShort = service.verifyRuc("12345");
        Optional<SunatRucInfo> resultNull = service.verifyRuc(null);

        assertTrue(resultShort.isEmpty());
        assertTrue(resultNull.isEmpty());
    }
}

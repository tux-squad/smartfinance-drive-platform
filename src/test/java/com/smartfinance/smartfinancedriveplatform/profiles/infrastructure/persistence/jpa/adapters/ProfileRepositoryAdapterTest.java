package com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.adapters;

import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.aggregates.Profile;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ProfileId;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.entities.ProfilePersistenceEntity;
import com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.persistence.jpa.repositories.SpringDataProfileRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProfileRepositoryAdapter.
 * Verifies persistence mapping and delegation to the Spring Data JPA layer.
 */
@ExtendWith(MockitoExtension.class)
class ProfileRepositoryAdapterTest {

    @Mock
    private SpringDataProfileRepository springDataProfileRepository;

    private ProfileRepositoryAdapter profileRepositoryAdapter;

    @BeforeEach
    void setUp() {
        profileRepositoryAdapter = new ProfileRepositoryAdapter(springDataProfileRepository);
    }

    @Test
    void testFindByIdFound() {
        UUID id = UUID.randomUUID();
        ProfilePersistenceEntity entity = new ProfilePersistenceEntity();
        entity.setId(id);
        entity.setUserId(UUID.randomUUID().toString());
        entity.setEmail("test@example.com");
        entity.setFullLegalNames("Test User");
        entity.setDateOfBirth(LocalDate.of(1995, 1, 1));
        entity.setPhoneCountryCode("+51");
        entity.setMobilePhone("912345678");
        entity.setMonthlyIncomeAmount(BigDecimal.valueOf(3000));
        entity.setMonthlyIncomeCurrency("PEN");
        entity.setEmploymentStatus("EMPLOYED");

        when(springDataProfileRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Profile> result = profileRepositoryAdapter.findById(new ProfileId(id));

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("Test User", result.get().getFullLegalNames());
    }

    @Test
    void testSaveProfile() {
        UUID id = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();
        Profile profile = new Profile(
            new ProfileId(id),
            new UserId(userId),
            "test@example.com",
            "71234567",
            "Test User",
            LocalDate.of(1995, 1, 1),
            "+51",
            "912345678",
            Money.of(3000, "PEN"),
            "EMPLOYED"
        );

        ProfilePersistenceEntity entity = new ProfilePersistenceEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setEmail("test@example.com");
        entity.setNationalId("71234567");
        entity.setFullLegalNames("Test User");
        entity.setDateOfBirth(LocalDate.of(1995, 1, 1));
        entity.setPhoneCountryCode("+51");
        entity.setMobilePhone("912345678");
        entity.setMonthlyIncomeAmount(BigDecimal.valueOf(3000));
        entity.setMonthlyIncomeCurrency("PEN");
        entity.setEmploymentStatus("EMPLOYED");

        when(springDataProfileRepository.findById(id)).thenReturn(Optional.empty());
        when(springDataProfileRepository.save(any())).thenReturn(entity);

        Profile result = profileRepositoryAdapter.save(profile);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(springDataProfileRepository, times(1)).save(any());
    }
}

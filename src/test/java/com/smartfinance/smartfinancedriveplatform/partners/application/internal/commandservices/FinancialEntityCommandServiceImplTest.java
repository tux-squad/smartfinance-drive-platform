package com.smartfinance.smartfinancedriveplatform.partners.application.internal.commandservices;

import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.FinancialEntity;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.LinkFinancialEntityToUserCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.UpdateFinancialEntityCommand;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.partners.domain.repositories.FinancialEntityRepository;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialEntityCommandServiceImpl Unit Tests")
class FinancialEntityCommandServiceImplTest {

    @Mock
    private FinancialEntityRepository financialEntityRepository;

    @InjectMocks
    private FinancialEntityCommandServiceImpl commandService;

    @Test
    @DisplayName("LinkCommand: returns existing entity if user already linked")
    void linkReturnsExistingIfUserAlreadyLinked() {
        FinancialEntity entity = new FinancialEntity(new FinancialEntityId(UUID.randomUUID()), "user-1", "20100047218", "Banco BCP", null, null, new ArrayList<>());
        when(financialEntityRepository.findByUserId("user-1")).thenReturn(Optional.of(entity));

        FinancialEntity result = commandService.handle(new LinkFinancialEntityToUserCommand("user-1", "20100047218", "Banco BCP"));

        assertEquals(entity, result);
        verify(financialEntityRepository, never()).save(any());
    }

    @Test
    @DisplayName("LinkCommand: links entity with matching RUC if unlinked")
    void linkClaimsEntityWithMatchingRucWhenUnlinked() {
        FinancialEntity entity = new FinancialEntity(new FinancialEntityId(UUID.randomUUID()), null, "20100047218", "Banco BCP", null, null, new ArrayList<>());
        when(financialEntityRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(financialEntityRepository.findByRuc("20100047218")).thenReturn(Optional.of(entity));
        when(financialEntityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FinancialEntity result = commandService.handle(new LinkFinancialEntityToUserCommand("user-1", "20100047218", "Banco BCP"));

        assertEquals("user-1", result.getUserId());
        verify(financialEntityRepository).save(entity);
    }

    @Test
    @DisplayName("LinkCommand: throws exception if entity with matching RUC is already linked to another user")
    void linkThrowsWhenRucBelongsToAnotherUser() {
        FinancialEntity entity = new FinancialEntity(new FinancialEntityId(UUID.randomUUID()), "user-other", "20100047218", "Banco BCP", null, null, new ArrayList<>());
        when(financialEntityRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(financialEntityRepository.findByRuc("20100047218")).thenReturn(Optional.of(entity));

        var ex = assertThrows(DomainValidationException.class, () ->
                commandService.handle(new LinkFinancialEntityToUserCommand("user-1", "20100047218", "Banco BCP")));

        assertEquals("partners.error.financialEntity.rucAlreadyLinked", ex.getMessage());
    }

    @Test
    @DisplayName("LinkCommand: throws exception if entity with matching name exists but has a different non-null RUC")
    void linkThrowsWhenFallbackByNameHasDifferentRuc() {
        FinancialEntity entity = new FinancialEntity(new FinancialEntityId(UUID.randomUUID()), null, "20555555555", "Banco BCP", null, null, new ArrayList<>());
        when(financialEntityRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(financialEntityRepository.findByRuc("20100047218")).thenReturn(Optional.empty());
        when(financialEntityRepository.findByName("Banco BCP")).thenReturn(Optional.of(entity));

        var ex = assertThrows(DomainValidationException.class, () ->
                commandService.handle(new LinkFinancialEntityToUserCommand("user-1", "20100047218", "Banco BCP")));

        assertEquals("partners.error.financialEntityRucMismatch", ex.getMessage());
    }

    @Test
    @DisplayName("LinkCommand: creates new entity when neither user nor RUC nor name matches existing")
    void linkCreatesNewEntityWhenNoMatch() {
        when(financialEntityRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(financialEntityRepository.findByRuc("20100047218")).thenReturn(Optional.empty());
        when(financialEntityRepository.findByName("Nuevo Banco")).thenReturn(Optional.empty());
        when(financialEntityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FinancialEntity result = commandService.handle(new LinkFinancialEntityToUserCommand("user-1", "20100047218", "Nuevo Banco"));

        assertNotNull(result);
        assertEquals("user-1", result.getUserId());
        assertEquals("20100047218", result.getRuc());
        assertEquals("Nuevo Banco", result.getName());
    }

    @Test
    @DisplayName("CreateCommand: throws exception if RUC already exists")
    void createThrowsWhenRucExists() {
        when(financialEntityRepository.existsByRuc("20100047218")).thenReturn(true);

        var ex = assertThrows(DomainValidationException.class, () ->
                commandService.handle(new CreateFinancialEntityCommand("user-1", "20100047218", "Banco Nuevo", null, null)));

        assertEquals("partners.error.financialEntityRucAlreadyExists", ex.getMessage());
    }

    @Test
    @DisplayName("UpdateCommand: throws exception if updated RUC belongs to another entity")
    void updateThrowsWhenRucBelongsToAnotherEntity() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        FinancialEntity entity1 = new FinancialEntity(new FinancialEntityId(id1), "user-1", "20100047218", "Banco 1", null, null, new ArrayList<>());
        FinancialEntity entity2 = new FinancialEntity(new FinancialEntityId(id2), "user-2", "20100099999", "Banco 2", null, null, new ArrayList<>());

        when(financialEntityRepository.findById(new FinancialEntityId(id1))).thenReturn(Optional.of(entity1));
        when(financialEntityRepository.findByRuc("20100099999")).thenReturn(Optional.of(entity2));

        var ex = assertThrows(DomainValidationException.class, () ->
                commandService.handle(new UpdateFinancialEntityCommand(new FinancialEntityId(id1), "user-1", "20100099999", "Banco 1", null, null)));

        assertEquals("partners.error.financialEntityRucAlreadyExists", ex.getMessage());
    }

    @Test
    @DisplayName("UpdateLogo: successfully updates and persists logo URL")
    void updateLogoSuccess() {
        UUID id = UUID.randomUUID();
        FinancialEntity entity = new FinancialEntity(new FinancialEntityId(id), "user-1", "20100047218", "Banco BCP", "old-logo.png", null, new ArrayList<>());
        when(financialEntityRepository.findById(new FinancialEntityId(id))).thenReturn(Optional.of(entity));
        when(financialEntityRepository.save(any(FinancialEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultOpt = commandService.updateLogo(new FinancialEntityId(id), "https://cdn.example.com/new-logo.png");

        assertTrue(resultOpt.isPresent());
        assertEquals("https://cdn.example.com/new-logo.png", resultOpt.get().getLogoUrl());
        verify(financialEntityRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("UpdateLogo: returns empty Optional when entity not found")
    void updateLogoNotFound() {
        UUID id = UUID.randomUUID();
        when(financialEntityRepository.findById(new FinancialEntityId(id))).thenReturn(Optional.empty());

        var resultOpt = commandService.updateLogo(new FinancialEntityId(id), "https://cdn.example.com/new-logo.png");

        assertTrue(resultOpt.isEmpty());
        verify(financialEntityRepository, never()).save(any());
    }

    @Test
    @DisplayName("UpdateBanner: successfully updates and persists banner URL")
    void updateBannerSuccess() {
        UUID id = UUID.randomUUID();
        FinancialEntity entity = new FinancialEntity(new FinancialEntityId(id), "user-1", "20100047218", "Banco BCP", null, "old-banner.png", new ArrayList<>());
        when(financialEntityRepository.findById(new FinancialEntityId(id))).thenReturn(Optional.of(entity));
        when(financialEntityRepository.save(any(FinancialEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultOpt = commandService.updateBanner(new FinancialEntityId(id), "https://cdn.example.com/new-banner.png");

        assertTrue(resultOpt.isPresent());
        assertEquals("https://cdn.example.com/new-banner.png", resultOpt.get().getBannerUrl());
        verify(financialEntityRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("UpdateBanner: returns empty Optional when entity not found")
    void updateBannerNotFound() {
        UUID id = UUID.randomUUID();
        when(financialEntityRepository.findById(new FinancialEntityId(id))).thenReturn(Optional.empty());

        var resultOpt = commandService.updateBanner(new FinancialEntityId(id), "https://cdn.example.com/new-banner.png");

        assertTrue(resultOpt.isEmpty());
        verify(financialEntityRepository, never()).save(any());
    }
}

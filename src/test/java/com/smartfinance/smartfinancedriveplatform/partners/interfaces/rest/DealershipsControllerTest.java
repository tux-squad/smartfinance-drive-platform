package com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest;

import com.smartfinance.smartfinancedriveplatform.catalog.application.queryservices.VehicleQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.application.commandservices.DealershipCommandService;
import com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.storage.DealershipImageStorageService;
import com.smartfinance.smartfinancedriveplatform.partners.application.queryservices.DealershipQueryService;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.aggregates.Dealership;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetAllDealershipsQuery;
import com.smartfinance.smartfinancedriveplatform.partners.domain.model.queries.GetDealershipByUserIdQuery;
import com.smartfinance.smartfinancedriveplatform.partners.interfaces.rest.resources.CreateUpdateDealershipResource;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("DealershipsController Unit Tests")
class DealershipsControllerTest {

    private DealershipCommandService commandService;
    private DealershipQueryService queryService;
    private DealershipImageStorageService imageStorageService;
    private VehicleQueryService vehicleQueryService;
    private DealershipsController controller;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        commandService = mock(DealershipCommandService.class);
        queryService = mock(DealershipQueryService.class);
        imageStorageService = mock(DealershipImageStorageService.class);
        vehicleQueryService = mock(VehicleQueryService.class);
        controller = new DealershipsController(commandService, queryService, imageStorageService, vehicleQueryService);

        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getRequiredCurrentUserId).thenReturn("dealer-user-1");
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("Should return all active dealerships paginated")
    void shouldReturnAllDealershipsPaginated() {
        Dealership dealership = new Dealership(
                "dealer-user-1",
                "20601234567",
                "Autoland Peru",
                "Av. Javier Prado 1234",
                "+51987654321",
                "contacto@autoland.pe",
                "https://autoland.pe",
                "Concesionaria líder en venta de autos",
                "Lun-Vie 9am-6pm",
                null,
                null
        );
        when(queryService.handle(any(GetAllDealershipsQuery.class), any())).thenReturn(new PageImpl<>(List.of(dealership)));

        var response = controller.getAllDealerships(null, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should return my dealership profile")
    void shouldReturnMyDealershipProfile() {
        Dealership dealership = new Dealership(
                "dealer-user-1",
                "20601234567",
                "Autoland Peru",
                "Av. Javier Prado 1234",
                "+51987654321",
                "contacto@autoland.pe",
                "https://autoland.pe",
                "Concesionaria líder en venta de autos",
                "Lun-Vie 9am-6pm",
                null,
                null
        );
        when(queryService.handle(any(GetDealershipByUserIdQuery.class))).thenReturn(Optional.of(dealership));

        var response = controller.getMyDealership();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Autoland Peru");
    }

    @Test
    @DisplayName("Should create or update my dealership with logo and banner")
    void shouldCreateOrUpdateMyDealershipWithLogoAndBanner() {
        Dealership dealership = new Dealership(
                "dealer-user-1",
                "20601234567",
                "Autoland Peru",
                "Av. Javier Prado 1234",
                "+51987654321",
                "contacto@autoland.pe",
                "https://autoland.pe",
                "Concesionaria líder en venta de autos",
                "Lun-Vie 9am-6pm",
                "https://cdn.example.com/logo.png",
                "https://cdn.example.com/banner.png"
        );
        when(commandService.handle(any(com.smartfinance.smartfinancedriveplatform.partners.domain.model.commands.CreateDealershipCommand.class)))
                .thenReturn(Optional.of(dealership));

        var resource = new CreateUpdateDealershipResource(
                "20601234567",
                "Autoland Peru",
                "Av. Javier Prado 1234",
                "+51987654321",
                "contacto@autoland.pe",
                "https://autoland.pe",
                "Concesionaria líder en venta de autos",
                "Lun-Vie 9am-6pm",
                "https://cdn.example.com/logo.png",
                "https://cdn.example.com/banner.png"
        );

        var response = controller.createOrUpdateMyDealership(resource);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().logoUrl()).isEqualTo("https://cdn.example.com/logo.png");
        assertThat(response.getBody().bannerUrl()).isEqualTo("https://cdn.example.com/banner.png");
    }

    @Test
    @DisplayName("Should upload logo and delete old one in correct order")
    void shouldUploadLogoSuccessfullyAfterDeletingOldOne() {
        Dealership dealership = new Dealership(
                "dealer-user-1",
                "20601234567",
                "Autoland Peru",
                "Av. Javier Prado 1234",
                "+51987654321",
                "contacto@autoland.pe",
                "https://autoland.pe",
                "Desc",
                "Hours",
                "https://cdn.example.com/old-logo.png",
                null
        );
        Dealership updated = new Dealership(
                "dealer-user-1",
                "20601234567",
                "Autoland Peru",
                "Av. Javier Prado 1234",
                "+51987654321",
                "contacto@autoland.pe",
                "https://autoland.pe",
                "Desc",
                "Hours",
                "https://cdn.example.com/new-logo.png",
                null
        );

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "logo.png", "image/png", "sample".getBytes());

        when(queryService.handle(any(GetDealershipByUserIdQuery.class))).thenReturn(Optional.of(dealership));
        when(imageStorageService.uploadDealershipImage(file, "logos")).thenReturn("https://cdn.example.com/new-logo.png");
        when(commandService.updateLogo(dealership.getId(), "https://cdn.example.com/new-logo.png")).thenReturn(Optional.of(updated));

        var response = controller.uploadLogo(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().logoUrl()).isEqualTo("https://cdn.example.com/new-logo.png");

        var inOrder = Mockito.inOrder(imageStorageService, commandService);
        inOrder.verify(imageStorageService).uploadDealershipImage(file, "logos");
        inOrder.verify(commandService).updateLogo(dealership.getId(), "https://cdn.example.com/new-logo.png");
        inOrder.verify(imageStorageService).deleteDealershipImage("https://cdn.example.com/old-logo.png");
    }

    @Test
    @DisplayName("Should upload banner and delete old one in correct order")
    void shouldUploadBannerSuccessfullyAfterDeletingOldOne() {
        Dealership dealership = new Dealership(
                "dealer-user-1",
                "20601234567",
                "Autoland Peru",
                "Av. Javier Prado 1234",
                "+51987654321",
                "contacto@autoland.pe",
                "https://autoland.pe",
                "Desc",
                "Hours",
                null,
                "https://cdn.example.com/old-banner.png"
        );
        Dealership updated = new Dealership(
                "dealer-user-1",
                "20601234567",
                "Autoland Peru",
                "Av. Javier Prado 1234",
                "+51987654321",
                "contacto@autoland.pe",
                "https://autoland.pe",
                "Desc",
                "Hours",
                null,
                "https://cdn.example.com/new-banner.png"
        );

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "banner.png", "image/png", "sample".getBytes());

        when(queryService.handle(any(GetDealershipByUserIdQuery.class))).thenReturn(Optional.of(dealership));
        when(imageStorageService.uploadDealershipImage(file, "banners")).thenReturn("https://cdn.example.com/new-banner.png");
        when(commandService.updateBanner(dealership.getId(), "https://cdn.example.com/new-banner.png")).thenReturn(Optional.of(updated));

        var response = controller.uploadBanner(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().bannerUrl()).isEqualTo("https://cdn.example.com/new-banner.png");

        var inOrder = Mockito.inOrder(imageStorageService, commandService);
        inOrder.verify(imageStorageService).uploadDealershipImage(file, "banners");
        inOrder.verify(commandService).updateBanner(dealership.getId(), "https://cdn.example.com/new-banner.png");
        inOrder.verify(imageStorageService).deleteDealershipImage("https://cdn.example.com/old-banner.png");
    }

    @Test
    @DisplayName("Should reject invalid logoUrl and bannerUrl formats in CreateUpdateDealershipResource")
    void shouldRejectInvalidUrlsInDealershipResource() {
        var validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        var resource = new CreateUpdateDealershipResource(
                "20601234567",
                "Autoland Peru",
                "Av. Javier Prado 1234",
                "+51987654321",
                "contacto@autoland.pe",
                "https://autoland.pe",
                "Desc",
                "Hours",
                "ftp://invalid-logo",
                "javascript:void(0)"
        );

        var violations = validator.validate(resource);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("logoUrl"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("bannerUrl"));
    }
}

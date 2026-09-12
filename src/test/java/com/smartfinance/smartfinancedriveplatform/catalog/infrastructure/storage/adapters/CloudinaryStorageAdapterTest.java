package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.storage.adapters;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryStorageAdapterTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryStorageAdapter cloudinaryStorageAdapter;

    @BeforeEach
    void setUp() {
        cloudinaryStorageAdapter = new CloudinaryStorageAdapter(cloudinary);
    }

    @Test
    void testUploadVehicleImageSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "car.png", "image/png", "fake image content".getBytes());
        String expectedUrl = "https://res.cloudinary.com/jzoqodzv/image/upload/v1/smartfinance/vehicles/car.png";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(ObjectUtils.asMap("secure_url", expectedUrl));

        String actualUrl = cloudinaryStorageAdapter.uploadVehicleImage(file);

        assertEquals(expectedUrl, actualUrl);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    void testUploadVehicleImageEmptyFileThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/png", new byte[0]);

        assertThrows(DomainValidationException.class, () -> cloudinaryStorageAdapter.uploadVehicleImage(emptyFile));
    }

    @Test
    void testUploadVehicleImageInvalidMimeTypeThrowsException() {
        MockMultipartFile pdfFile = new MockMultipartFile("file", "document.pdf", "application/pdf", "fake pdf".getBytes());

        assertThrows(DomainValidationException.class, () -> cloudinaryStorageAdapter.uploadVehicleImage(pdfFile));
    }

    @Test
    void testUploadVehicleImageExceedsSizeThrowsException() {
        byte[] largeBytes = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile("file", "large.png", "image/png", largeBytes);

        assertThrows(DomainValidationException.class, () -> cloudinaryStorageAdapter.uploadVehicleImage(largeFile));
    }

    @Test
    void testDeleteVehicleImageSuccess() throws IOException {
        String imageUrl = "https://res.cloudinary.com/jzoqodzv/image/upload/v1/smartfinance/vehicles/test_car.png";
        when(cloudinary.uploader()).thenReturn(uploader);

        cloudinaryStorageAdapter.deleteVehicleImage(imageUrl);

        verify(uploader, times(1)).destroy(eq("smartfinance/vehicles/test_car"), any());
    }
}

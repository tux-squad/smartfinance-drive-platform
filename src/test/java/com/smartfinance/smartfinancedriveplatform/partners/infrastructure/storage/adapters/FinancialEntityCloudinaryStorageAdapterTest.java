package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.storage.adapters;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialEntityCloudinaryStorageAdapter Unit Tests")
class FinancialEntityCloudinaryStorageAdapterTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private FinancialEntityCloudinaryStorageAdapter storageAdapter;

    @BeforeEach
    void setUp() {
        storageAdapter = new FinancialEntityCloudinaryStorageAdapter(cloudinary);
    }

    @Test
    @DisplayName("Should successfully upload financial entity logo to Cloudinary")
    void testUploadLogoSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "bcp-logo.png", "image/png", "sample content".getBytes());
        String expectedUrl = "https://res.cloudinary.com/demo/image/upload/v1/smartfinance/financial-entities/logos/bcp-logo.png";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(ObjectUtils.asMap("secure_url", expectedUrl));

        String resultUrl = storageAdapter.uploadFinancialEntityImage(file, "logos");

        assertEquals(expectedUrl, resultUrl);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    @DisplayName("Should successfully upload financial entity banner to Cloudinary")
    void testUploadBannerSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "bcp-banner.jpg", "image/jpeg", "sample content".getBytes());
        String expectedUrl = "https://res.cloudinary.com/demo/image/upload/v1/smartfinance/financial-entities/banners/bcp-banner.jpg";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(ObjectUtils.asMap("secure_url", expectedUrl));

        String resultUrl = storageAdapter.uploadFinancialEntityImage(file, "banners");

        assertEquals(expectedUrl, resultUrl);
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    @DisplayName("Should throw exception when uploading empty file")
    void testUploadEmptyFileThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThrows(DomainValidationException.class, () -> storageAdapter.uploadFinancialEntityImage(emptyFile, "logos"));
    }

    @Test
    @DisplayName("Should throw exception when uploading unsupported MIME type")
    void testUploadInvalidMimeTypeThrowsException() {
        MockMultipartFile pdfFile = new MockMultipartFile("file", "document.pdf", "application/pdf", "fake pdf".getBytes());

        assertThrows(DomainValidationException.class, () -> storageAdapter.uploadFinancialEntityImage(pdfFile, "logos"));
    }

    @Test
    @DisplayName("Should throw exception when file size exceeds 10MB limit")
    void testUploadExceedsSizeThrowsException() {
        byte[] largeBytes = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile("file", "large.png", "image/png", largeBytes);

        assertThrows(DomainValidationException.class, () -> storageAdapter.uploadFinancialEntityImage(largeFile, "logos"));
    }

    @Test
    @DisplayName("Should successfully delete image from Cloudinary by extracting publicId")
    void testDeleteImageSuccess() throws IOException {
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1234567/smartfinance/financial-entities/logos/old-logo.png";
        when(cloudinary.uploader()).thenReturn(uploader);

        storageAdapter.deleteFinancialEntityImage(imageUrl);

        verify(uploader, times(1)).destroy(eq("smartfinance/financial-entities/logos/old-logo"), any());
    }

    @Test
    @DisplayName("Should safely handle null or blank image URL during deletion without exception")
    void testDeleteImageNullOrBlank() {
        assertDoesNotThrow(() -> storageAdapter.deleteFinancialEntityImage(null));
        assertDoesNotThrow(() -> storageAdapter.deleteFinancialEntityImage(""));
        verifyNoInteractions(cloudinary);
    }
}

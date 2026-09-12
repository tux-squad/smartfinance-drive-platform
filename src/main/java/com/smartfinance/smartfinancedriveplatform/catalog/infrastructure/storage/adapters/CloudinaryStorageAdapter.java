package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.storage.adapters;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smartfinance.smartfinancedriveplatform.catalog.application.outboundservices.storage.VehicleImageStorageService;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Adapter implementation for VehicleImageStorageService using Cloudinary SDK.
 */
@Service
public class CloudinaryStorageAdapter implements VehicleImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageAdapter.class);

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    private final Cloudinary cloudinary;

    public CloudinaryStorageAdapter(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadVehicleImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DomainValidationException("catalog.error.vehicle.imageFile.empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new DomainValidationException("catalog.error.vehicle.imageFile.exceedsSize");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new DomainValidationException("catalog.error.vehicle.imageFile.invalidType");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "smartfinance/vehicles",
                            "resource_type", "image"
                    )
            );
            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new DomainValidationException("catalog.error.vehicle.imageUpload.failed");
            }
            return secureUrl.toString();
        } catch (IOException e) {
            log.error("Failed to upload vehicle image to Cloudinary", e);
            throw new DomainValidationException("catalog.error.vehicle.imageUpload.failed");
        }
    }

    @Override
    public void deleteVehicleImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String publicId = extractPublicId(imageUrl);
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Successfully deleted old vehicle image from Cloudinary with publicId: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete vehicle image from Cloudinary with publicId: {}", publicId, e);
        }
    }

    /**
     * Helper to extract public ID from Cloudinary URL (e.g. smartfinance/vehicles/abc123).
     */
    private String extractPublicId(String imageUrl) {
        try {
            int folderIdx = imageUrl.indexOf("smartfinance/vehicles/");
            if (folderIdx == -1) {
                return null;
            }
            String pathWithExtension = imageUrl.substring(folderIdx);
            int lastDotIdx = pathWithExtension.lastIndexOf('.');
            if (lastDotIdx != -1) {
                return pathWithExtension.substring(0, lastDotIdx);
            }
            return pathWithExtension;
        } catch (Exception e) {
            log.warn("Could not parse Cloudinary publicId from URL: {}", imageUrl);
            return null;
        }
    }
}

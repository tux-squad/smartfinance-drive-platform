package com.smartfinance.smartfinancedriveplatform.catalog.infrastructure.storage.adapters;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smartfinance.smartfinancedriveplatform.catalog.application.outboundservices.storage.VehicleImageStorageService;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Adapter implementation for VehicleImageStorageService using Cloudinary SDK.
 */
@Service
public class CloudinaryStorageAdapter implements VehicleImageStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageAdapter(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadVehicleImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DomainValidationException("catalog.error.vehicle.imageFile.empty");
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
            throw new DomainValidationException("catalog.error.vehicle.imageUpload.ioException: " + e.getMessage());
        }
    }
}

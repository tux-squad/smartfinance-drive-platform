package com.smartfinance.smartfinancedriveplatform.catalog.application.outboundservices.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Outbound service port for vehicle image storage operations.
 */
public interface VehicleImageStorageService {

    /**
     * Uploads a vehicle image file to storage and returns its secure URL.
     *
     * @param file The multipart image file.
     * @return The uploaded image HTTPS URL string.
     */
    String uploadVehicleImage(MultipartFile file);
}

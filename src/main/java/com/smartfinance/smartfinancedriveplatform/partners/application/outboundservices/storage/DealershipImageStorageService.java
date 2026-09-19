package com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Outbound service port for dealership media storage (logo/banner).
 */
public interface DealershipImageStorageService {

    /**
     * Uploads a dealership logo or banner image file to storage.
     *
     * @param file   The multipart image file.
     * @param folder Target folder type ("logos" or "banners").
     * @return Uploaded image HTTPS URL.
     */
    String uploadDealershipImage(MultipartFile file, String folder);

    /**
     * Deletes an existing image from storage.
     *
     * @param imageUrl The image URL.
     */
    void deleteDealershipImage(String imageUrl);
}

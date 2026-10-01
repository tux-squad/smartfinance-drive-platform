package com.smartfinance.smartfinancedriveplatform.partners.infrastructure.storage.adapters;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smartfinance.smartfinancedriveplatform.partners.application.outboundservices.storage.FinancialEntityImageStorageService;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Adapter implementation for FinancialEntityImageStorageService using Cloudinary SDK.
 */
@Service
public class FinancialEntityCloudinaryStorageAdapter implements FinancialEntityImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(FinancialEntityCloudinaryStorageAdapter.class);
    private static final String BASE_FOLDER = "smartfinance/financial-entities";

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    private final Cloudinary cloudinary;

    public FinancialEntityCloudinaryStorageAdapter(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadFinancialEntityImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new DomainValidationException("partners.error.financialEntity.imageFile.empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new DomainValidationException("partners.error.financialEntity.imageFile.exceedsSize");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new DomainValidationException("partners.error.financialEntity.imageFile.invalidType");
        }

        String targetFolder = BASE_FOLDER + "/" + (folder != null ? folder : "misc");

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", targetFolder,
                            "resource_type", "image"
                    )
            );
            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new DomainValidationException("partners.error.financialEntity.imageUpload.failed");
            }
            return secureUrl.toString();
        } catch (IOException e) {
            log.error("Failed to upload financial entity image to Cloudinary", e);
            throw new DomainValidationException("partners.error.financialEntity.imageUpload.failed");
        }
    }

    @Override
    public void deleteFinancialEntityImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String publicId = extractPublicId(imageUrl);
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Successfully deleted financial entity image from Cloudinary with publicId: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete financial entity image from Cloudinary with publicId: {}", publicId, e);
        }
    }

    private String extractPublicId(String imageUrl) {
        try {
            int uploadIdx = imageUrl.indexOf("/upload/");
            if (uploadIdx == -1) {
                return null;
            }

            String pathAfterUpload = imageUrl.substring(uploadIdx + "/upload/".length());
            String pathWithoutVersion = pathAfterUpload.replaceFirst("^v\\d+/", "");

            int lastDotIdx = pathWithoutVersion.lastIndexOf('.');
            if (lastDotIdx != -1) {
                return pathWithoutVersion.substring(0, lastDotIdx);
            }
            return pathWithoutVersion;
        } catch (Exception e) {
            log.warn("Could not parse Cloudinary publicId from URL: {}", imageUrl);
            return null;
        }
    }
}

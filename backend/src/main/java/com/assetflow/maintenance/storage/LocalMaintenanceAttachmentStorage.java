package com.assetflow.maintenance.storage;

import com.assetflow.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Local filesystem implementation of {@link MaintenanceAttachmentStorage}.
 *
 * The upload directory defaults to {@code ./uploads/maintenance} and can be
 * overridden by setting {@code app.maintenance.attachment.upload-dir} in
 * {@code application.properties} (report this property to the team leader).
 *
 * Files are stored with a UUID-based name to prevent collisions and path
 * traversal attacks.  Only a relative reference is returned — never an
 * absolute server path.
 */
@Slf4j
@Service
public class LocalMaintenanceAttachmentStorage implements MaintenanceAttachmentStorage {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png");

    /**
     * Configurable via application.properties:
     *   app.maintenance.attachment.upload-dir=./uploads/maintenance
     *
     * Team leader should add this property to application.properties.
     */
    @Value("${app.maintenance.attachment.upload-dir:./uploads/maintenance}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Attachment file must not be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException(
                    "Invalid file type. Only JPEG and PNG images are allowed. Received: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException(
                    "File size exceeds the maximum allowed limit of 5 MB. Received: "
                    + (file.getSize() / (1024 * 1024)) + " MB");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String extension = contentType.equals("image/png") ? ".png" : ".jpg";
            String fileName = UUID.randomUUID() + extension;

            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return only the relative path under the upload-dir root
            String relativePath = "maintenance/" + fileName;
            log.info("Attachment stored at relative path: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to store maintenance attachment", e);
            throw new RuntimeException("Failed to store attachment: " + e.getMessage(), e);
        }
    }
}

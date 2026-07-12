package com.assetflow.maintenance.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for storing maintenance request attachments.
 * Keeps the storage mechanism replaceable (local filesystem, S3, etc.)
 * without touching the maintenance service logic.
 *
 * Do NOT make this a general shared service — it is scoped exclusively
 * to the maintenance module.
 */
public interface MaintenanceAttachmentStorage {

    /**
     * Validates and stores the uploaded file.
     *
     * <ul>
     *   <li>Accepted MIME types: image/jpeg, image/png</li>
     *   <li>Maximum size: 5 MB</li>
     * </ul>
     *
     * @param file the uploaded file from the HTTP multipart request
     * @return a relative path / reference to the stored file (never an absolute server path)
     * @throws com.assetflow.exception.BadRequestException if validation fails
     */
    String store(MultipartFile file);
}

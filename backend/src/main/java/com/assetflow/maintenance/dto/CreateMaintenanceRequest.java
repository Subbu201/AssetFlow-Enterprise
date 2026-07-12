package com.assetflow.maintenance.dto;

import com.assetflow.maintenance.entity.MaintenancePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMaintenanceRequest {

    @NotNull(message = "assetId is required")
    private Long assetId;

    @NotBlank(message = "issueDescription is required")
    private String issueDescription;

    @NotNull(message = "priority is required")
    private MaintenancePriority priority;

    /** Optional URL / path set after file upload. Usually left null here. */
    private String attachmentUrl;
}

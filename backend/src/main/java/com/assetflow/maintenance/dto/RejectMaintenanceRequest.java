package com.assetflow.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectMaintenanceRequest {

    @NotBlank(message = "rejectionReason is required")
    private String rejectionReason;
}

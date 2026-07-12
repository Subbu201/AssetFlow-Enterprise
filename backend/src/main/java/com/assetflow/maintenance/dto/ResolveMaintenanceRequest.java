package com.assetflow.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResolveMaintenanceRequest {

    @NotBlank(message = "resolutionNotes are required")
    private String resolutionNotes;
}

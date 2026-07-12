package com.assetflow.maintenance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTechnicianRequest {

    @NotNull(message = "technicianUserId is required")
    private Long technicianUserId;

    private String comments;
}

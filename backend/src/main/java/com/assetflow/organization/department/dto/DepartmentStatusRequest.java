package com.assetflow.organization.department.dto;

import com.assetflow.common.RecordStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartmentStatusRequest {
    @NotNull(message = "Status is required")
    private RecordStatus status;
}

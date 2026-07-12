package com.assetflow.transfer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTransferRequest {
    @NotNull
    private Long allocationId;

    @NotNull
    private Long assetId;

    @NotNull
    private Long requestedByUserId;

    private Long targetEmployeeId;
    private Long targetDepartmentId;
    private String reason;
    private LocalDateTime requestedAt;

    public CreateTransferRequest() {}

    public CreateTransferRequest(Long allocationId, Long assetId, Long requestedByUserId, Long targetEmployeeId, Long targetDepartmentId, String reason, LocalDateTime requestedAt) {
        this.allocationId = allocationId;
        this.assetId = assetId;
        this.requestedByUserId = requestedByUserId;
        this.targetEmployeeId = targetEmployeeId;
        this.targetDepartmentId = targetDepartmentId;
        this.reason = reason;
        this.requestedAt = requestedAt;
    }
}

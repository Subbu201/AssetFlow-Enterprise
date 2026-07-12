package com.assetflow.transfer.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransferResponse {
    private Long id;
    private Long allocationId;
    private Long assetId;
    private Long requestedByUserId;
    private Long targetEmployeeId;
    private Long targetDepartmentId;
    private String reason;
    private String status;
    private Long reviewedByUserId;
    private LocalDateTime reviewedAt;
    private String reviewComments;
}

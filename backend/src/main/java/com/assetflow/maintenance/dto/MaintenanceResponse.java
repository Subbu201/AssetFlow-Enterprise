package com.assetflow.maintenance.dto;

import com.assetflow.common.MaintenanceStatus;
import com.assetflow.maintenance.entity.MaintenancePriority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceResponse {

    private Long id;
    private String requestNumber;
    private Long assetId;
    private Long raisedByUserId;
    private String issueDescription;
    private MaintenancePriority priority;
    private String attachmentUrl;
    private MaintenanceStatus status;
    private Long approvedByUserId;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private Long technicianUserId;
    private LocalDateTime technicianAssignedAt;
    private LocalDateTime repairStartedAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

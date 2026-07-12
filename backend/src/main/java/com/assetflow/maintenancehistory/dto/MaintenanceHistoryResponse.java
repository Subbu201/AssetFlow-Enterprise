package com.assetflow.maintenancehistory.dto;

import com.assetflow.common.MaintenanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceHistoryResponse {

    private Long id;
    private Long maintenanceRequestId;
    private Long assetId;
    private MaintenanceStatus previousStatus;
    private MaintenanceStatus newStatus;
    private Long actionByUserId;
    private String comments;
    private LocalDateTime eventTime;
    private LocalDateTime createdAt;
}

package com.assetflow.activitylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    private Long id;
    private Long actorUserId;
    private String action;
    private String entityType;
    private Long entityId;
    private String description;
    private LocalDateTime actionTime;
}

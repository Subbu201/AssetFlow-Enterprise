package com.assetflow.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDiscrepancyResponse {
    private Long id;
    private Long auditCycleId;
    private Long auditItemId;
    private Long assetId;
    private String discrepancyType;
    private String description;
    private String status;
    private Long resolvedByUserId;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;
}

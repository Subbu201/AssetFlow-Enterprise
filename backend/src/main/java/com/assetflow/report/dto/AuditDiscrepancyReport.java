package com.assetflow.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDiscrepancyReport {
    private Long discrepancyId;
    private Long auditCycleId;
    private Long auditItemId;
    private Long assetId;
    private String discrepancyType;
    private String status;
}

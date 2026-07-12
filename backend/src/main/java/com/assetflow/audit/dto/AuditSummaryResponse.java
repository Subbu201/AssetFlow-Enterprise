package com.assetflow.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditSummaryResponse {
    private Long auditCycleId;
    private long totalItems;
    private long verifiedItems;
    private long missingItems;
    private long damagedItems;
    private long pendingItems;
    private long discrepancyCount;
}

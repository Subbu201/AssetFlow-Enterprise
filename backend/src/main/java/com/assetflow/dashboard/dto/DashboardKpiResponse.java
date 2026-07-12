package com.assetflow.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpiResponse {
    private long availableAssets;
    private long allocatedAssets;
    private long maintenanceToday;
    private long activeBookings;
    private long pendingTransfers;
    private long upcomingReturns;
    private long overdueReturns;
    private long openAuditDiscrepancies;
}

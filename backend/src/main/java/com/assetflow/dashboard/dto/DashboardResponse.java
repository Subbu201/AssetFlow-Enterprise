package com.assetflow.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private DashboardKpiResponse kpis;
    private List<DashboardItemResponse> upcomingReturns;
    private List<DashboardItemResponse> overdueReturns;
    private List<DashboardItemResponse> activeBookings;
    private List<DashboardItemResponse> recentMaintenance;
}

package com.assetflow.dashboard.gateway;

import com.assetflow.dashboard.dto.DashboardItemResponse;

import java.util.List;

public interface MaintenanceDashboardGateway {
    long countMaintenanceToday();
    List<DashboardItemResponse> getRecentMaintenance(int limit);
}

package com.assetflow.dashboard.gateway;

import com.assetflow.dashboard.dto.DashboardItemResponse;

import java.util.List;

public interface AllocationDashboardGateway {
    long countPendingTransfers();
    long countUpcomingReturns();
    long countOverdueReturns();
    List<DashboardItemResponse> getUpcomingReturns(int limit);
    List<DashboardItemResponse> getOverdueReturns(int limit);
}

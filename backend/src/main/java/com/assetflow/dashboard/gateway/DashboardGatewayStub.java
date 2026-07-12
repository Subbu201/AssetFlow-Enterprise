package com.assetflow.dashboard.gateway;

import com.assetflow.dashboard.dto.DashboardItemResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DashboardGatewayStub implements AssetDashboardGateway, AllocationDashboardGateway, BookingDashboardGateway, MaintenanceDashboardGateway {

    @Override
    public long countAvailableAssets() {
        return 0;
    }

    @Override
    public long countAllocatedAssets() {
        return 0;
    }

    @Override
    public long countPendingTransfers() {
        return 0;
    }

    @Override
    public long countUpcomingReturns() {
        return 0;
    }

    @Override
    public long countOverdueReturns() {
        return 0;
    }

    @Override
    public List<DashboardItemResponse> getUpcomingReturns(int limit) {
        return Collections.emptyList();
    }

    @Override
    public List<DashboardItemResponse> getOverdueReturns(int limit) {
        return Collections.emptyList();
    }

    @Override
    public long countActiveBookings() {
        return 0;
    }

    @Override
    public List<DashboardItemResponse> getActiveBookings(int limit) {
        return Collections.emptyList();
    }

    @Override
    public long countMaintenanceToday() {
        return 0;
    }

    @Override
    public List<DashboardItemResponse> getRecentMaintenance(int limit) {
        return Collections.emptyList();
    }
}

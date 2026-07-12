package com.assetflow.dashboard;

import com.assetflow.activitylog.ActivityLogService;
import com.assetflow.dashboard.dto.DashboardKpiResponse;
import com.assetflow.dashboard.dto.DashboardResponse;
import com.assetflow.dashboard.gateway.AllocationDashboardGateway;
import com.assetflow.dashboard.gateway.AssetDashboardGateway;
import com.assetflow.dashboard.gateway.BookingDashboardGateway;
import com.assetflow.dashboard.gateway.MaintenanceDashboardGateway;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class DashboardService {

    private final AssetDashboardGateway assetGateway;
    private final AllocationDashboardGateway allocationGateway;
    private final BookingDashboardGateway bookingGateway;
    private final MaintenanceDashboardGateway maintenanceGateway;
    public DashboardService(AssetDashboardGateway assetGateway,
                            AllocationDashboardGateway allocationGateway,
                            BookingDashboardGateway bookingGateway,
                            MaintenanceDashboardGateway maintenanceGateway,
                            ActivityLogService activityLogService) {
        this.assetGateway = assetGateway;
        this.allocationGateway = allocationGateway;
        this.bookingGateway = bookingGateway;
        this.maintenanceGateway = maintenanceGateway;
    }

    public DashboardResponse getDashboard() {
        long availableAssets = assetGateway.countAvailableAssets();
        long allocatedAssets = assetGateway.countAllocatedAssets();
        long maintenanceToday = maintenanceGateway.countMaintenanceToday();
        long activeBookings = bookingGateway.countActiveBookings();
        long pendingTransfers = allocationGateway.countPendingTransfers();
        long upcomingReturns = allocationGateway.countUpcomingReturns();
        long overdueReturns = allocationGateway.countOverdueReturns();
        long openAuditDiscrepancies = 0;

        DashboardKpiResponse kpis = DashboardKpiResponse.builder()
                .availableAssets(availableAssets)
                .allocatedAssets(allocatedAssets)
                .maintenanceToday(maintenanceToday)
                .activeBookings(activeBookings)
                .pendingTransfers(pendingTransfers)
                .upcomingReturns(upcomingReturns)
                .overdueReturns(overdueReturns)
                .openAuditDiscrepancies(openAuditDiscrepancies)
                .build();

        return DashboardResponse.builder()
                .kpis(kpis)
                .upcomingReturns(safeList(allocationGateway.getUpcomingReturns(5)))
                .overdueReturns(safeList(allocationGateway.getOverdueReturns(5)))
                .activeBookings(safeList(bookingGateway.getActiveBookings(5)))
                .recentMaintenance(safeList(maintenanceGateway.getRecentMaintenance(5)))
                .build();
    }

    private <T> java.util.List<T> safeList(java.util.List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}

package com.assetflow.dashboard;

import com.assetflow.dashboard.dto.DashboardItemResponse;
import com.assetflow.dashboard.dto.DashboardResponse;
import com.assetflow.dashboard.gateway.AllocationDashboardGateway;
import com.assetflow.dashboard.gateway.AssetDashboardGateway;
import com.assetflow.dashboard.gateway.BookingDashboardGateway;
import com.assetflow.dashboard.gateway.MaintenanceDashboardGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AssetDashboardGateway assetGateway;

    @Mock
    private AllocationDashboardGateway allocationGateway;

    @Mock
    private BookingDashboardGateway bookingGateway;

    @Mock
    private MaintenanceDashboardGateway maintenanceGateway;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(assetGateway, allocationGateway, bookingGateway, maintenanceGateway);
    }

    @Test
    void getDashboardReturnsAggregatedDataAndEmptyLists() {
        when(assetGateway.countAvailableAssets()).thenReturn(8L);
        when(assetGateway.countAllocatedAssets()).thenReturn(5L);
        when(maintenanceGateway.countMaintenanceToday()).thenReturn(1L);
        when(bookingGateway.countActiveBookings()).thenReturn(2L);
        when(allocationGateway.countPendingTransfers()).thenReturn(3L);
        when(allocationGateway.countUpcomingReturns()).thenReturn(4L);
        when(allocationGateway.countOverdueReturns()).thenReturn(0L);
        when(allocationGateway.getUpcomingReturns(5)).thenReturn(null);
        when(allocationGateway.getOverdueReturns(5)).thenReturn(null);
        when(bookingGateway.getActiveBookings(5)).thenReturn(List.of(new DashboardItemResponse(1L, "Active booking", "Test", "Details", "2026-07-12")));
        when(maintenanceGateway.getRecentMaintenance(5)).thenReturn(null);

        DashboardResponse response = service.getDashboard();

        assertNotNull(response);
        assertNotNull(response.getKpis());
        assertEquals(8L, response.getKpis().getAvailableAssets());
        assertEquals(5L, response.getKpis().getAllocatedAssets());
        assertEquals(1L, response.getKpis().getMaintenanceToday());
        assertEquals(2L, response.getKpis().getActiveBookings());
        assertEquals(3L, response.getKpis().getPendingTransfers());
        assertEquals(4L, response.getKpis().getUpcomingReturns());
        assertEquals(0L, response.getKpis().getOverdueReturns());
        assertNotNull(response.getUpcomingReturns());
        assertTrue(response.getUpcomingReturns().isEmpty());
        assertNotNull(response.getOverdueReturns());
        assertTrue(response.getOverdueReturns().isEmpty());
        assertNotNull(response.getActiveBookings());
        assertEquals(1, response.getActiveBookings().size());
        assertNotNull(response.getRecentMaintenance());
        assertTrue(response.getRecentMaintenance().isEmpty());
    }
}

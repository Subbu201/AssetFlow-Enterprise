package com.assetflow.dashboard.gateway;

import com.assetflow.dashboard.dto.DashboardItemResponse;

import java.util.List;

public interface BookingDashboardGateway {
    long countActiveBookings();
    List<DashboardItemResponse> getActiveBookings(int limit);
}

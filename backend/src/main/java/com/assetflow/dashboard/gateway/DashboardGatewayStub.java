package com.assetflow.dashboard.gateway;

import com.assetflow.asset.Asset;
import com.assetflow.asset.AssetRepository;
import com.assetflow.common.AssetStatus;
import com.assetflow.common.BookingStatus;
import com.assetflow.common.MaintenanceStatus;
import com.assetflow.allocation.AssetAllocation;
import com.assetflow.allocation.AssetAllocationRepository;
import com.assetflow.booking.entity.ResourceBooking;
import com.assetflow.booking.repository.ResourceBookingRepository;
import com.assetflow.maintenance.entity.MaintenanceRequest;
import com.assetflow.maintenance.repository.MaintenanceRequestRepository;
import com.assetflow.dashboard.dto.DashboardItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DashboardGatewayStub implements AssetDashboardGateway, AllocationDashboardGateway, BookingDashboardGateway, MaintenanceDashboardGateway {

    private final AssetRepository assetRepository;
    private final AssetAllocationRepository allocationRepository;
    private final ResourceBookingRepository bookingRepository;
    private final MaintenanceRequestRepository maintenanceRepository;

    @Override
    public long countAvailableAssets() {
        return assetRepository.countByStatus(AssetStatus.AVAILABLE);
    }

    @Override
    public long countAllocatedAssets() {
        return assetRepository.countByStatus(AssetStatus.ALLOCATED);
    }

    @Override
    public long countPendingTransfers() {
        return allocationRepository.countByStatus("PENDING");
    }

    @Override
    public long countUpcomingReturns() {
        return allocationRepository.countByStatus("ACTIVE");
    }

    @Override
    public long countOverdueReturns() {
        return allocationRepository.countByStatusAndExpectedReturnDateBefore("ACTIVE", LocalDate.now());
    }

    @Override
    public List<DashboardItemResponse> getUpcomingReturns(int limit) {
        List<AssetAllocation> active = allocationRepository.findByStatus("ACTIVE");
        return active.stream()
                .limit(limit)
                .map(alloc -> {
                    String assetName = assetRepository.findById(alloc.getAssetId())
                            .map(Asset::getName)
                            .orElse("Asset #" + alloc.getAssetId());
                    return DashboardItemResponse.builder()
                            .id(alloc.getId())
                            .title(assetName + " allocated")
                            .subtitle("Expected Return: " + (alloc.getExpectedReturnDate() != null ? alloc.getExpectedReturnDate().toString() : "N/A"))
                            .date(alloc.getAllocationDate() != null ? alloc.getAllocationDate().toString() : "")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardItemResponse> getOverdueReturns(int limit) {
        List<AssetAllocation> overdue = allocationRepository.findByStatusAndExpectedReturnDateBefore("ACTIVE", LocalDate.now());
        return overdue.stream()
                .limit(limit)
                .map(alloc -> {
                    String assetName = assetRepository.findById(alloc.getAssetId())
                            .map(Asset::getName)
                            .orElse("Asset #" + alloc.getAssetId());
                    return DashboardItemResponse.builder()
                            .id(alloc.getId())
                            .title(assetName + " OVERDUE")
                            .subtitle("Expected: " + (alloc.getExpectedReturnDate() != null ? alloc.getExpectedReturnDate().toString() : "N/A"))
                            .date(alloc.getExpectedReturnDate() != null ? alloc.getExpectedReturnDate().toString() : "")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public long countActiveBookings() {
        return bookingRepository.countByStatusIn(List.of(BookingStatus.UPCOMING, BookingStatus.ONGOING));
    }

    @Override
    public List<DashboardItemResponse> getActiveBookings(int limit) {
        // Since we want active bookings, we can fetch all and filter or limit
        List<ResourceBooking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.UPCOMING || b.getStatus() == BookingStatus.ONGOING)
                .limit(limit)
                .map(b -> {
                    String assetName = assetRepository.findById(b.getAssetId())
                            .map(Asset::getName)
                            .orElse("Asset #" + b.getAssetId());
                    return DashboardItemResponse.builder()
                            .id(b.getId())
                            .title(assetName + " booking")
                            .subtitle(b.getStartTime() != null ? b.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "")
                            .date(b.getStartTime() != null ? b.getStartTime().toLocalDate().toString() : "")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public long countMaintenanceToday() {
        return maintenanceRepository.countByStatusIn(List.of(
                MaintenanceStatus.PENDING,
                MaintenanceStatus.APPROVED,
                MaintenanceStatus.TECHNICIAN_ASSIGNED,
                MaintenanceStatus.IN_PROGRESS
        ));
    }

    @Override
    public List<DashboardItemResponse> getRecentMaintenance(int limit) {
        List<MaintenanceRequest> requests = maintenanceRepository.findAll();
        return requests.stream()
                .limit(limit)
                .map(mr -> {
                    String assetName = assetRepository.findById(mr.getAssetId())
                            .map(Asset::getName)
                            .orElse("Asset #" + mr.getAssetId());
                    return DashboardItemResponse.builder()
                            .id(mr.getId())
                            .title(assetName + " Maintenance: " + mr.getStatus())
                            .subtitle(mr.getIssueDescription())
                            .date(mr.getCreatedAt() != null ? mr.getCreatedAt().toLocalDate().toString() : "")
                            .build();
                })
                .collect(Collectors.toList());
    }
}

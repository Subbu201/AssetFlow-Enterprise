package com.assetflow.report;

import com.assetflow.exception.ForbiddenException;
import com.assetflow.common.Role;
import com.assetflow.report.dto.AssetStatusReport;
import com.assetflow.report.dto.AuditDiscrepancyReport;
import com.assetflow.report.dto.DepartmentAllocationReport;
import com.assetflow.report.dto.MaintenanceFrequencyReport;
import com.assetflow.report.dto.OverdueAllocationReport;
import com.assetflow.report.dto.ResourceUtilizationReport;
import com.assetflow.report.gateway.AllocationReportGateway;
import com.assetflow.report.gateway.AssetReportGateway;
import com.assetflow.report.gateway.BookingReportGateway;
import com.assetflow.report.gateway.MaintenanceReportGateway;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final AssetReportGateway assetReportGateway;
    private final AllocationReportGateway allocationReportGateway;
    private final BookingReportGateway bookingReportGateway;
    private final MaintenanceReportGateway maintenanceReportGateway;

    public ReportService(AssetReportGateway assetReportGateway,
                         AllocationReportGateway allocationReportGateway,
                         BookingReportGateway bookingReportGateway,
                         MaintenanceReportGateway maintenanceReportGateway) {
        this.assetReportGateway = assetReportGateway;
        this.allocationReportGateway = allocationReportGateway;
        this.bookingReportGateway = bookingReportGateway;
        this.maintenanceReportGateway = maintenanceReportGateway;
    }

    public List<DepartmentAllocationReport> getDepartmentAllocationReport() {
        authorizeOrganizationWide();
        return safeList(assetReportGateway.departmentAllocationSummary());
    }

    public List<AssetStatusReport> getAssetStatusReport() {
        authorizeOrganizationWide();
        return safeList(assetReportGateway.assetStatusSummary());
    }

    public List<MaintenanceFrequencyReport> getMaintenanceFrequencyReport() {
        authorizeOrganizationWide();
        return safeList(maintenanceReportGateway.maintenanceFrequencySummary());
    }

    public List<ResourceUtilizationReport> getResourceUtilizationReport() {
        authorizeOrganizationWide();
        return safeList(bookingReportGateway.resourceUtilizationSummary());
    }

    public List<OverdueAllocationReport> getOverdueAllocationReport() {
        authorizeOrganizationWide();
        return safeList(allocationReportGateway.overdueAllocationReport());
    }

    public List<AuditDiscrepancyReport> getAuditDiscrepancyReport() {
        authorizeOrganizationWide();
        return safeList(maintenanceReportGateway.auditDiscrepancyReport());
    }

    private void authorizeOrganizationWide() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }
        boolean allowed = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name()) || a.getAuthority().equals("ROLE_" + Role.ASSET_MANAGER.name()));
        if (!allowed) {
            throw new ForbiddenException("Access denied");
        }
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}

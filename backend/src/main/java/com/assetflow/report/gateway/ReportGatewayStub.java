package com.assetflow.report.gateway;

import com.assetflow.report.dto.AssetStatusReport;
import com.assetflow.report.dto.AuditDiscrepancyReport;
import com.assetflow.report.dto.DepartmentAllocationReport;
import com.assetflow.report.dto.MaintenanceFrequencyReport;
import com.assetflow.report.dto.OverdueAllocationReport;
import com.assetflow.report.dto.ResourceUtilizationReport;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ReportGatewayStub implements AssetReportGateway, AllocationReportGateway, BookingReportGateway, MaintenanceReportGateway {

    @Override
    public List<DepartmentAllocationReport> departmentAllocationSummary() {
        return Collections.emptyList();
    }

    @Override
    public List<AssetStatusReport> assetStatusSummary() {
        return Collections.emptyList();
    }

    @Override
    public List<OverdueAllocationReport> overdueAllocationReport() {
        return Collections.emptyList();
    }

    @Override
    public List<ResourceUtilizationReport> resourceUtilizationSummary() {
        return Collections.emptyList();
    }

    @Override
    public List<MaintenanceFrequencyReport> maintenanceFrequencySummary() {
        return Collections.emptyList();
    }

    @Override
    public List<AuditDiscrepancyReport> auditDiscrepancyReport() {
        return Collections.emptyList();
    }
}

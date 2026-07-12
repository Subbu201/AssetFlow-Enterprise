package com.assetflow.report.gateway;

import com.assetflow.report.dto.MaintenanceFrequencyReport;
import com.assetflow.report.dto.AuditDiscrepancyReport;

import java.util.List;

public interface MaintenanceReportGateway {
    List<MaintenanceFrequencyReport> maintenanceFrequencySummary();
    List<AuditDiscrepancyReport> auditDiscrepancyReport();
}

package com.assetflow.report.gateway;

import com.assetflow.report.dto.OverdueAllocationReport;

import java.util.List;

public interface AllocationReportGateway {
    List<OverdueAllocationReport> overdueAllocationReport();
}

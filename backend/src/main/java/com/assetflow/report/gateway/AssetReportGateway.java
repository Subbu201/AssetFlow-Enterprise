package com.assetflow.report.gateway;

import com.assetflow.report.dto.AssetStatusReport;
import com.assetflow.report.dto.DepartmentAllocationReport;

import java.util.List;

public interface AssetReportGateway {
    List<DepartmentAllocationReport> departmentAllocationSummary();
    List<AssetStatusReport> assetStatusSummary();
}

package com.assetflow.report.gateway;

import com.assetflow.report.dto.ResourceUtilizationReport;

import java.util.List;

public interface BookingReportGateway {
    List<ResourceUtilizationReport> resourceUtilizationSummary();
}

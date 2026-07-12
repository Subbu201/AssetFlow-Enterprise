package com.assetflow.report;

import com.assetflow.common.ApiResponse;
import com.assetflow.report.dto.AssetStatusReport;
import com.assetflow.report.dto.AuditDiscrepancyReport;
import com.assetflow.report.dto.DepartmentAllocationReport;
import com.assetflow.report.dto.MaintenanceFrequencyReport;
import com.assetflow.report.dto.OverdueAllocationReport;
import com.assetflow.report.dto.ResourceUtilizationReport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/department-allocation")
    public ApiResponse<List<DepartmentAllocationReport>> getDepartmentAllocation() {
        return ApiResponse.success("Department allocation report returned", service.getDepartmentAllocationReport());
    }

    @GetMapping("/assets-by-status")
    public ApiResponse<List<AssetStatusReport>> getAssetsByStatus() {
        return ApiResponse.success("Asset status report returned", service.getAssetStatusReport());
    }

    @GetMapping("/maintenance-frequency")
    public ApiResponse<List<MaintenanceFrequencyReport>> getMaintenanceFrequency() {
        return ApiResponse.success("Maintenance frequency report returned", service.getMaintenanceFrequencyReport());
    }

    @GetMapping("/resource-utilization")
    public ApiResponse<List<ResourceUtilizationReport>> getResourceUtilization() {
        return ApiResponse.success("Resource utilization report returned", service.getResourceUtilizationReport());
    }

    @GetMapping("/overdue-allocations")
    public ApiResponse<List<OverdueAllocationReport>> getOverdueAllocations() {
        return ApiResponse.success("Overdue allocation report returned", service.getOverdueAllocationReport());
    }

    @GetMapping("/audit-discrepancies")
    public ApiResponse<List<AuditDiscrepancyReport>> getAuditDiscrepancies() {
        return ApiResponse.success("Audit discrepancy report returned", service.getAuditDiscrepancyReport());
    }

    @GetMapping(value = "/{reportName}/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> exportCsv(
            @PathVariable @NotBlank String reportName,
            @RequestParam @NotBlank @Pattern(regexp = "(?i)csv", message = "Only CSV export is supported") String format) {
        String csv;

        switch (reportName) {
            case "department-allocation" -> csv = toCsv(service.getDepartmentAllocationReport(), "departmentId,allocatedAssets,availableAssets");
            case "assets-by-status" -> csv = toCsv(service.getAssetStatusReport(), "status,count");
            case "maintenance-frequency" -> csv = toCsv(service.getMaintenanceFrequencyReport(), "maintenanceRequestId,assetName,maintenanceCount");
            case "resource-utilization" -> csv = toCsv(service.getResourceUtilizationReport(), "resourceType,usedCount,totalCount");
            case "overdue-allocations" -> csv = toCsv(service.getOverdueAllocationReport(), "allocationId,assetId,employeeId,dueDate,status");
            case "audit-discrepancies" -> csv = toCsv(service.getAuditDiscrepancyReport(), "discrepancyId,auditCycleId,auditItemId,assetId,discrepancyType,status");
            default -> csv = "";
        }

        if (csv.isBlank()) {
            return ResponseEntity.badRequest().body("Unknown report name");
        }
        return ResponseEntity.ok(csv);
    }

    private String toCsv(List<?> rows, String header) {
        StringBuilder builder = new StringBuilder();
        builder.append(header).append("\n");
        for (Object row : safeList(rows)) {
            builder.append(serializeRow(row)).append("\n");
        }
        return builder.toString();
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    private String serializeRow(Object row) {
        return java.util.Arrays.stream(row.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .map(field -> {
                    try {
                        Object value = field.get(row);
                        return escapeCsv(value == null ? "" : value.toString());
                    } catch (IllegalAccessException e) {
                        return "";
                    }
                })
                .collect(Collectors.joining(","));
    }

    private String escapeCsv(String value) {
        String escaped = value.replace("\"", "\"\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        if (escaped.contains(",") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}

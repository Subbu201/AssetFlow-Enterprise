package com.assetflow.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueAllocationReport {
    private Long allocationId;
    private Long assetId;
    private Long employeeId;
    private String dueDate;
    private String status;
}

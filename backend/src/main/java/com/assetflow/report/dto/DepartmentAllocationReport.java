package com.assetflow.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentAllocationReport {
    private Long departmentId;
    private long allocatedAssets;
    private long availableAssets;
}

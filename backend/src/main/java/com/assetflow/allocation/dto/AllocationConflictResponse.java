package com.assetflow.allocation.dto;

import lombok.Data;

@Data
public class AllocationConflictResponse {
    private Long assetId;
    private String assetTag;
    private Long currentEmployeeId;
    private Long currentDepartmentId;
    private Long currentAllocationId;
    private String message;
}

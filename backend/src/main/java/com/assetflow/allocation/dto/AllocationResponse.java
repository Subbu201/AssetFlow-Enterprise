package com.assetflow.allocation.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AllocationResponse {
    private Long id;
    private Long assetId;
    private Long employeeId;
    private Long departmentId;
    private Long allocatedByUserId;
    private LocalDate allocationDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private String status;
    private String checkoutCondition;
    private String notes;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

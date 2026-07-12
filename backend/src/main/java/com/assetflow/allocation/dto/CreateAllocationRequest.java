package com.assetflow.allocation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAllocationRequest {
    @NotNull
    private Long assetId;

    private Long employeeId;
    private Long departmentId;

    @NotNull
    private Long allocatedByUserId;

    private LocalDate allocationDate;
    private LocalDate expectedReturnDate;
    private String notes;

    public CreateAllocationRequest() {}

    public CreateAllocationRequest(Long assetId, Long employeeId, Long departmentId, LocalDate allocationDate, LocalDate expectedReturnDate, String notes) {
        this.assetId = assetId;
        this.employeeId = employeeId;
        this.departmentId = departmentId;
        this.allocatedByUserId = 1L;
        this.allocationDate = allocationDate;
        this.expectedReturnDate = expectedReturnDate;
        this.notes = notes;
    }
}

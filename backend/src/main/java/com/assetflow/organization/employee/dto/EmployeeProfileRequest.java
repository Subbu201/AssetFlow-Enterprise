package com.assetflow.organization.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeProfileRequest {
    @NotBlank(message = "Employee Code is required")
    private String employeeCode;

    private Long departmentId;

    @NotBlank(message = "Designation is required")
    private String designation;

    private String phone;
    
    private String joiningDate; // or LocalDate
}

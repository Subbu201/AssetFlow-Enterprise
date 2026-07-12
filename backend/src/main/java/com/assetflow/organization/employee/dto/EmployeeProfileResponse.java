package com.assetflow.organization.employee.dto;

import com.assetflow.common.RecordStatus;
import com.assetflow.common.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmployeeProfileResponse {
    private Long id;
    private Long userAccountId;
    private String employeeCode;
    private Long departmentId;
    private String designation;
    private String phone;
    private LocalDate joiningDate;
    private RecordStatus status;
    
    // Additional populated fields from UserAccount
    private String email;
    private String fullName;
    private Role role;
    private java.util.List<AllocatedAssetResponse> allocatedAssets;

    @Data
    @Builder
    public static class AllocatedAssetResponse {
        private Long id;
        private String name;
        private String assetTag;
        private String serialNumber;
    }
}

package com.assetflow.organization.department.dto;

import com.assetflow.common.RecordStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Long parentDepartmentId;
    private Long departmentHeadUserId;
    private RecordStatus status;
}

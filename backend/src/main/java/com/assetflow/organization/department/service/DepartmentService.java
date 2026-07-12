package com.assetflow.organization.department.service;

import com.assetflow.organization.department.dto.CreateDepartmentRequest;
import com.assetflow.organization.department.dto.DepartmentResponse;
import com.assetflow.organization.department.dto.DepartmentStatusRequest;
import com.assetflow.organization.department.dto.UpdateDepartmentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    DepartmentResponse createDepartment(CreateDepartmentRequest request);
    Page<DepartmentResponse> getDepartments(String name, String code, Pageable pageable);
    DepartmentResponse getDepartment(Long id);
    DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request);
    DepartmentResponse updateDepartmentStatus(Long id, DepartmentStatusRequest request);
    // Tree add-on and head assignment can be added here
}

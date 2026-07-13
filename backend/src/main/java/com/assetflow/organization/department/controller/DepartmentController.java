package com.assetflow.organization.department.controller;

import com.assetflow.common.ApiResponse;
import com.assetflow.organization.department.dto.*;
import com.assetflow.organization.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/departments")
@PreAuthorize("hasAnyRole('ADMIN', 'ASSET_MANAGER')")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Department created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DepartmentResponse>>> getDepartments(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            Pageable pageable) {
        Page<DepartmentResponse> responses = departmentService.getDepartments(name, code, pageable);
        return ResponseEntity.ok(ApiResponse.success("Departments fetched successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartment(@PathVariable Long id) {
        DepartmentResponse response = departmentService.getDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department fetched successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateDepartmentRequest request) {
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartmentStatus(
            @PathVariable Long id, 
            @Valid @RequestBody DepartmentStatusRequest request) {
        DepartmentResponse response = departmentService.updateDepartmentStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Department status updated successfully", response));
    }
}

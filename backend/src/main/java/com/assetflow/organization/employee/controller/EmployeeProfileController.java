package com.assetflow.organization.employee.controller;

import com.assetflow.common.ApiResponse;
import com.assetflow.organization.employee.dto.EmployeeProfileResponse;
import com.assetflow.organization.employee.dto.UpdateRoleRequest;
import com.assetflow.organization.employee.service.EmployeeProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/employees")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class EmployeeProfileController {

    private final EmployeeProfileService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeProfileResponse>>> getEmployees(
            @RequestParam(required = false) String employeeCode,
            Pageable pageable) {
        Page<EmployeeProfileResponse> responses = employeeService.getEmployees(employeeCode, pageable);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> getEmployee(@PathVariable Long id) {
        EmployeeProfileResponse response = employeeService.getEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee fetched successfully", response));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        EmployeeProfileResponse response = employeeService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee role updated successfully", response));
    }
}

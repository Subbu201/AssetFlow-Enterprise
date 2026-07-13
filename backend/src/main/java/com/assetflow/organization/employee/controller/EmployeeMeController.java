package com.assetflow.organization.employee.controller;

import com.assetflow.common.ApiResponse;
import com.assetflow.organization.employee.dto.EmployeeProfileResponse;
import com.assetflow.organization.employee.service.EmployeeProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeMeController {

    private final EmployeeProfileService employeeService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> getMyProfile(org.springframework.security.core.Authentication authentication) {
        com.assetflow.security.jwt.CustomUserDetails userDetails = (com.assetflow.security.jwt.CustomUserDetails) authentication.getPrincipal();
        EmployeeProfileResponse response = employeeService.getProfileByUserAccountId(userDetails.getUserAccount().getId());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", response));
    }
}

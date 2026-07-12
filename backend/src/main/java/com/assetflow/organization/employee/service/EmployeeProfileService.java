package com.assetflow.organization.employee.service;

import com.assetflow.auth.entity.UserAccount;
import com.assetflow.auth.repository.UserRepository;
import com.assetflow.common.Role;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.organization.department.repository.DepartmentRepository;
import com.assetflow.organization.employee.dto.EmployeeProfileResponse;
import com.assetflow.organization.employee.dto.UpdateRoleRequest;
import com.assetflow.organization.employee.entity.EmployeeProfile;
import com.assetflow.organization.employee.repository.EmployeeProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeProfileService {

    private final EmployeeProfileRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public Page<EmployeeProfileResponse> getEmployees(String employeeCode, Pageable pageable) {
        String searchCode = employeeCode == null ? "" : employeeCode;
        return employeeRepository.findByEmployeeCodeContainingIgnoreCase(searchCode, pageable)
                .map(this::mapToResponse);
    }

    public EmployeeProfileResponse getEmployee(Long id) {
        EmployeeProfile employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeProfileResponse updateRole(Long id, UpdateRoleRequest request) {
        EmployeeProfile profile = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        UserAccount user = userRepository.findById(profile.getUserAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot assign ADMIN role through API");
        }

        if (request.getRole() == Role.DEPARTMENT_HEAD && profile.getDepartmentId() == null) {
            throw new BadRequestException("DEPARTMENT_HEAD must belong to a department");
        }

        // If removing DEPARTMENT_HEAD, should we clear department head ref? Yes, handled elsewhere or here
        user.setRole(request.getRole());
        userRepository.save(user);

        return mapToResponse(profile);
    }

    private EmployeeProfileResponse mapToResponse(EmployeeProfile profile) {
        UserAccount user = userRepository.findById(profile.getUserAccountId()).orElse(null);
        
        return EmployeeProfileResponse.builder()
                .id(profile.getId())
                .userAccountId(profile.getUserAccountId())
                .employeeCode(profile.getEmployeeCode())
                .departmentId(profile.getDepartmentId())
                .designation(profile.getDesignation())
                .phone(profile.getPhone())
                .joiningDate(profile.getJoiningDate())
                .status(profile.getStatus())
                .email(user != null ? user.getEmail() : null)
                .fullName(user != null ? user.getFullName() : null)
                .role(user != null ? user.getRole() : null)
                .build();
    }
}

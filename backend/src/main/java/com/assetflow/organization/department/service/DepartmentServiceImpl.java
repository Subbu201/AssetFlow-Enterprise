package com.assetflow.organization.department.service;

import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.organization.department.dto.*;
import com.assetflow.organization.department.entity.Department;
import com.assetflow.organization.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new ConflictException("Department name already exists");
        }
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Department code already exists");
        }

        if (request.getParentDepartmentId() != null) {
            departmentRepository.findById(request.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent department not found"));
        }

        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .parentDepartmentId(request.getParentDepartmentId())
                .build();

        department = departmentRepository.save(department);
        return mapToResponse(department);
    }

    @Override
    public Page<DepartmentResponse> getDepartments(String name, String code, Pageable pageable) {
        String searchName = name == null ? "" : name;
        String searchCode = code == null ? "" : code;
        return departmentRepository.findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase(searchName, searchCode, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public DepartmentResponse getDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        return mapToResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (!department.getName().equals(request.getName()) && departmentRepository.existsByName(request.getName())) {
            throw new ConflictException("Department name already exists");
        }
        
        if (!department.getCode().equals(request.getCode()) && departmentRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Department code already exists");
        }

        if (request.getParentDepartmentId() != null && request.getParentDepartmentId().equals(id)) {
            throw new ConflictException("Department cannot be its own parent");
        }

        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());
        department.setParentDepartmentId(request.getParentDepartmentId());

        return mapToResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartmentStatus(Long id, DepartmentStatusRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        
        department.setStatus(request.getStatus());
        return mapToResponse(departmentRepository.save(department));
    }

    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .description(department.getDescription())
                .parentDepartmentId(department.getParentDepartmentId())
                .departmentHeadUserId(department.getDepartmentHeadUserId())
                .status(department.getStatus())
                .build();
    }
}

package com.assetflow.organization.department.repository;

import com.assetflow.organization.department.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
    
    Page<Department> findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase(String name, String code, Pageable pageable);
}

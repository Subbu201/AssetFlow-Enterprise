package com.assetflow.organization.employee.repository;

import com.assetflow.organization.employee.entity.EmployeeProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByUserAccountId(Long userAccountId);
    Optional<EmployeeProfile> findByUserAccountId(Long userAccountId);
    Page<EmployeeProfile> findByEmployeeCodeContainingIgnoreCase(String employeeCode, Pageable pageable);
}

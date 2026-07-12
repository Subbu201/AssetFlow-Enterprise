package com.assetflow.allocation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssetAllocationRepository extends JpaRepository<AssetAllocation, Long> {
    Optional<AssetAllocation> findFirstByAssetIdAndStatus(String assetId, String status);
    List<AssetAllocation> findByAssetId(Long assetId);
    List<AssetAllocation> findByEmployeeId(Long employeeId);
    List<AssetAllocation> findByDepartmentId(Long departmentId);
    List<AssetAllocation> findByStatus(String status);
    List<AssetAllocation> findByStatusAndExpectedReturnDateBefore(String status, LocalDate date);
    boolean existsByAssetIdAndStatus(Long assetId, String status);
    Optional<AssetAllocation> findFirstByAssetIdAndStatusIn(Long assetId, List<String> statuses);
    long countByStatus(String status);
    long countByStatusAndExpectedReturnDateBefore(String status, LocalDate date);
}

package com.assetflow.maintenance.repository;

import com.assetflow.common.MaintenanceStatus;
import com.assetflow.maintenance.entity.MaintenanceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRequestRepository
        extends JpaRepository<MaintenanceRequest, Long>, JpaSpecificationExecutor<MaintenanceRequest> {
    long countByStatusIn(List<com.assetflow.common.MaintenanceStatus> statuses);

    /**
     * Checks whether the asset already has an active (non-terminal) maintenance request.
     * Active statuses: PENDING, APPROVED, TECHNICIAN_ASSIGNED, IN_PROGRESS.
     */
    @Query("""
            SELECT COUNT(mr) > 0 FROM MaintenanceRequest mr
            WHERE mr.assetId = :assetId
              AND mr.status IN :statuses
            """)
    boolean existsActiveRequestForAsset(
            @Param("assetId") Long assetId,
            @Param("statuses") List<MaintenanceStatus> statuses);

    /**
     * Finds all active maintenance requests for an asset — used by booking module
     * to check cross-module state.
     */
    @Query("""
            SELECT mr FROM MaintenanceRequest mr
            WHERE mr.assetId = :assetId
              AND mr.status IN :statuses
            """)
    List<MaintenanceRequest> findActiveRequestsForAsset(
            @Param("assetId") Long assetId,
            @Param("statuses") List<MaintenanceStatus> statuses);

    Page<MaintenanceRequest> findByRaisedByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<MaintenanceRequest> findByAssetIdOrderByCreatedAtDesc(Long assetId, Pageable pageable);

    @Query("""
            SELECT mr FROM MaintenanceRequest mr
            WHERE mr.status = com.assetflow.common.MaintenanceStatus.PENDING
            ORDER BY mr.createdAt DESC
            """)
    Page<MaintenanceRequest> findPending(Pageable pageable);

    /**
     * Safe request-number generation: finds the maximum numeric suffix of all
     * existing request numbers.
     * Returns null when no requests exist yet.
     *
     * Format: MR-NNNN  (e.g. MR-0001)
     */
    @Query("SELECT MAX(CAST(SUBSTRING(mr.requestNumber, 4) AS int)) FROM MaintenanceRequest mr")
    Optional<Integer> findMaxRequestNumberSuffix();
}

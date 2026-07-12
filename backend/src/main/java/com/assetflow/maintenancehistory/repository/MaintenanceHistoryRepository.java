package com.assetflow.maintenancehistory.repository;

import com.assetflow.maintenancehistory.entity.MaintenanceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceHistoryRepository extends JpaRepository<MaintenanceHistory, Long> {

    /** Returns history entries for a maintenance request, newest first. */
    Page<MaintenanceHistory> findByMaintenanceRequestIdOrderByEventTimeDesc(
            Long maintenanceRequestId, Pageable pageable);
}

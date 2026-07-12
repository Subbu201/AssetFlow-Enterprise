package com.assetflow.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditDiscrepancyRepository extends JpaRepository<AuditDiscrepancy, Long> {
    List<AuditDiscrepancy> findByAuditCycleId(Long auditCycleId);
    long countByAuditCycleIdAndStatus(String status);
}

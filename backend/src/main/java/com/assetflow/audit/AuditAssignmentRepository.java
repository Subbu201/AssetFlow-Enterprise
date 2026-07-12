package com.assetflow.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditAssignmentRepository extends JpaRepository<AuditAssignment, Long> {
    List<AuditAssignment> findByAuditCycleId(Long auditCycleId);
    boolean existsByAuditCycleIdAndAuditorUserId(Long auditCycleId, Long auditorUserId);
    List<AuditAssignment> findByAuditorUserId(Long auditorUserId);
}

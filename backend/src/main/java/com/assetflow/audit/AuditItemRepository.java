package com.assetflow.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditItemRepository extends JpaRepository<AuditItem, Long> {
    List<AuditItem> findByAuditCycleId(Long auditCycleId);
    boolean existsByAuditCycleId(Long auditCycleId);
    long countByAuditCycleIdAndVerificationStatusIsNull(Long auditCycleId);
    long countByAuditCycleId(Long auditCycleId);
    long countByAuditCycleIdAndVerificationStatus(Long auditCycleId, com.assetflow.common.AuditVerificationStatus status);
    java.util.Optional<AuditItem> findByIdAndAuditCycleId(Long id, Long auditCycleId);
}

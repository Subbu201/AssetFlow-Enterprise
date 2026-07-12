package com.assetflow.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuditCycleRepository extends JpaRepository<AuditCycle, Long> {
    Optional<AuditCycle> findByAuditCode(String auditCode);
}

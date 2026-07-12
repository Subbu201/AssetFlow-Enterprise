package com.assetflow.audit;

import com.assetflow.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_assignments")
public class AuditAssignment extends BaseEntity {

    @Column(nullable = false)
    private Long auditCycleId;

    @Column(nullable = false)
    private Long auditorUserId;

    @Column(nullable = false)
    private LocalDateTime assignedAt;
}

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
@Table(name = "audit_discrepancies")
public class AuditDiscrepancy extends BaseEntity {

    @Column(nullable = false)
    private Long auditCycleId;

    @Column(nullable = false)
    private Long auditItemId;

    @Column(nullable = false)
    private Long assetId;

    @Column(nullable = false)
    private String discrepancyType;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String status;

    private Long resolvedByUserId;

    private String resolutionNotes;

    private LocalDateTime resolvedAt;
}

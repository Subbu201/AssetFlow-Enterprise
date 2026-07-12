package com.assetflow.audit;

import com.assetflow.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_cycles")
public class AuditCycle extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String auditCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String scopeType;

    private Long departmentId;

    private String location;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Long createdByUserId;

    private Long closedByUserId;

    private LocalDateTime closedAt;

    @Version
    private Long version;
}

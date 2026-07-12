package com.assetflow.audit;

import com.assetflow.common.AssetStatus;
import com.assetflow.common.AuditVerificationStatus;
import com.assetflow.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_items")
public class AuditItem extends BaseEntity {

    @Column(nullable = false)
    private Long auditCycleId;

    @Column(nullable = false)
    private Long assetId;

    @Column(nullable = false)
    private String expectedLocation;

    private Long expectedDepartmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus expectedAssetStatus;

    @Enumerated(EnumType.STRING)
    private AuditVerificationStatus verificationStatus;

    private String actualLocation;

    private String actualCondition;

    private Long verifiedByUserId;

    private String remarks;

    private LocalDateTime verifiedAt;
}

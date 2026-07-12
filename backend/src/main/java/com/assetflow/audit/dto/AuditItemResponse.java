package com.assetflow.audit.dto;

import com.assetflow.common.AssetStatus;
import com.assetflow.common.AuditVerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditItemResponse {
    private Long id;
    private Long auditCycleId;
    private Long assetId;
    private String expectedLocation;
    private Long expectedDepartmentId;
    private AssetStatus expectedAssetStatus;
    private AuditVerificationStatus verificationStatus;
    private String actualLocation;
    private String actualCondition;
    private Long verifiedByUserId;
    private String remarks;
    private LocalDateTime verifiedAt;
}

package com.assetflow.audit.gateway;

import com.assetflow.common.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditAssetSnapshot {
    private Long assetId;
    private String location;
    private Long departmentId;
    private AssetStatus status;
}

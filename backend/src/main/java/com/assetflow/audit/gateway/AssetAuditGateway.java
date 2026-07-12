package com.assetflow.audit.gateway;

import java.util.List;

public interface AssetAuditGateway {
    List<AuditAssetSnapshot> findAssetsByDepartment(Long departmentId);
    List<AuditAssetSnapshot> findAssetsByLocation(String location);
    AuditAssetSnapshot getAsset(Long assetId);
    void markAssetLost(Long assetId, Long performedByUserId, String remarks);
    void markAssetDamaged(Long assetId, Long performedByUserId, String remarks);
}

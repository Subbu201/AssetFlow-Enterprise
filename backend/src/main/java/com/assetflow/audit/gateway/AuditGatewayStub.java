package com.assetflow.audit.gateway;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AuditGatewayStub implements AssetAuditGateway {

    @Override
    public List<AuditAssetSnapshot> findAssetsByDepartment(Long departmentId) {
        return Collections.emptyList();
    }

    @Override
    public List<AuditAssetSnapshot> findAssetsByLocation(String location) {
        return Collections.emptyList();
    }

    @Override
    public AuditAssetSnapshot getAsset(Long assetId) {
        return null;
    }

    @Override
    public void markAssetLost(Long assetId, Long performedByUserId, String remarks) {
        // no-op stub implementation
    }

    @Override
    public void markAssetDamaged(Long assetId, Long performedByUserId, String remarks) {
        // no-op stub implementation
    }
}

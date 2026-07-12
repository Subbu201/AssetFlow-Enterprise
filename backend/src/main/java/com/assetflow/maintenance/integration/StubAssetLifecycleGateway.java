package com.assetflow.maintenance.integration;

import com.assetflow.common.AssetStatus;
import com.assetflow.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory no-op stub for {@link AssetLifecycleGateway}.
 *
 * Activated automatically whenever no real implementation is present in the
 * Spring context (dev, default, and test profiles). Once Member 2 provides
 * a concrete {@link AssetLifecycleGateway} bean, this stub steps aside
 * automatically via {@code @ConditionalOnMissingBean} in config.
 *
 * Tests can pre-populate asset snapshots via {@link #registerAsset(AssetSnapshot)}.
 */
@Slf4j
public class StubAssetLifecycleGateway implements AssetLifecycleGateway {

    private final Map<Long, AssetSnapshot> store = new ConcurrentHashMap<>();

    /**
     * Pre-populate an asset snapshot for use in tests.
     */
    public void registerAsset(AssetSnapshot snapshot) {
        store.put(snapshot.getId(), snapshot);
    }

    public void clear() {
        store.clear();
    }

    @Override
    public AssetSnapshot getAsset(Long assetId) {
        AssetSnapshot snap = store.get(assetId);
        if (snap == null) {
            throw new ResourceNotFoundException("Asset not found: " + assetId);
        }
        return snap;
    }

    @Override
    public void markUnderMaintenance(Long assetId, Long performedByUserId, String reason) {
        AssetSnapshot existing = getAsset(assetId);
        store.put(assetId, AssetSnapshot.builder()
                .id(existing.getId())
                .assetTag(existing.getAssetTag())
                .name(existing.getName())
                .status(AssetStatus.UNDER_MAINTENANCE)
                .bookable(existing.isBookable())
                .build());
    }

    @Override
    public void markAvailable(Long assetId, Long performedByUserId, String reason) {
        AssetSnapshot existing = getAsset(assetId);
        store.put(assetId, AssetSnapshot.builder()
                .id(existing.getId())
                .assetTag(existing.getAssetTag())
                .name(existing.getName())
                .status(AssetStatus.AVAILABLE)
                .bookable(existing.isBookable())
                .build());
    }
}

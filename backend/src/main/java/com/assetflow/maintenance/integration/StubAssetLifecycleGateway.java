package com.assetflow.maintenance.integration;

import com.assetflow.common.AssetStatus;
import com.assetflow.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory stub implementation of {@link AssetLifecycleGateway}.
 * Loaded only when the "test" Spring profile is active.
 * Tests can pre-populate assets by calling {@link #registerAsset(AssetSnapshot)}.
 */
@Component
@Profile("test")
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

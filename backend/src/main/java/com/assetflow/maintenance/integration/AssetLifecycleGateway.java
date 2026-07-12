package com.assetflow.maintenance.integration;

/**
 * Gateway interface isolating this module from Member 2's AssetLifecycleService.
 * The integration leader will provide a real implementation that delegates to
 * AssetLifecycleService once Member 2's branch is merged.
 */
public interface AssetLifecycleGateway {

    /**
     * Returns a lightweight snapshot of the asset with its current status
     * and bookability flag.
     *
     * @param assetId the asset's primary key
     * @return AssetSnapshot, never null
     * @throws com.assetflow.exception.ResourceNotFoundException if the asset does not exist
     */
    AssetSnapshot getAsset(Long assetId);

    /**
     * Changes the asset status to UNDER_MAINTENANCE and records the audit event.
     *
     * @param assetId          the asset to transition
     * @param performedByUserId the user who triggered the change
     * @param reason           human-readable reason for the transition
     */
    void markUnderMaintenance(Long assetId, Long performedByUserId, String reason);

    /**
     * Changes the asset status back to AVAILABLE and records the audit event.
     *
     * @param assetId          the asset to transition
     * @param performedByUserId the user who triggered the change
     * @param reason           human-readable reason for the transition
     */
    void markAvailable(Long assetId, Long performedByUserId, String reason);
}

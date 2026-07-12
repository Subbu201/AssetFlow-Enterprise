package com.assetflow.maintenance.integration;

import com.assetflow.common.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable snapshot of an Asset used across module boundaries.
 * No JPA relationship — only IDs and status are carried.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetSnapshot {

    private Long id;
    private String assetTag;
    private String name;
    private AssetStatus status;
    /** True when the asset category allows shared / bookable usage. */
    private boolean bookable;
}

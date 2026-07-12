package com.assetflow.maintenance.entity;

/**
 * Priority levels for a maintenance request.
 * Kept inside the maintenance package — not added to any shared enum.
 */
public enum MaintenancePriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

package com.assetflow.maintenancehistory.entity;

import com.assetflow.common.BaseEntity;
import com.assetflow.common.MaintenanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Audit trail of every status transition for a maintenance request.
 * No JPA relationship to MaintenanceRequest — only the ID is stored.
 */
@Entity
@Table(name = "maintenance_history",
        indexes = {
            @Index(name = "idx_mh_request_id", columnList = "maintenanceRequestId"),
            @Index(name = "idx_mh_asset_id",   columnList = "assetId"),
            @Index(name = "idx_mh_event_time",  columnList = "eventTime")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceHistory extends BaseEntity {

    /** FK to the maintenance request — stored as ID only. */
    @Column(nullable = false)
    private Long maintenanceRequestId;

    /** Denormalised for easier querying — asset ID from the maintenance request. */
    @Column(nullable = false)
    private Long assetId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MaintenanceStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceStatus newStatus;

    /** FK to the user who performed the action. */
    @Column(nullable = false)
    private Long actionByUserId;

    @Column(length = 1000)
    private String comments;

    @Column(nullable = false)
    private LocalDateTime eventTime;
}

package com.assetflow.assethistory;

import com.assetflow.common.AssetStatus;
import com.assetflow.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset_history")
@Getter
@Setter
public class AssetHistory extends BaseEntity {

    private Long assetId;

    @Column(nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    private AssetStatus previousStatus;

    @Enumerated(EnumType.STRING)
    private AssetStatus newStatus;

    private String previousValue;

    private String newValue;

    private String remarks;

    private Long performedByUserId;

    private LocalDateTime eventTime;
}

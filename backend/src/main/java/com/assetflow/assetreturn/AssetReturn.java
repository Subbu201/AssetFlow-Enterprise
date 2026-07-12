package com.assetflow.assetreturn;

import com.assetflow.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset_returns")
@Getter
@Setter
public class AssetReturn extends BaseEntity {

    private Long allocationId;
    private Long assetId;
    private Long requestedByUserId;
    private LocalDateTime requestedAt;
    private String returnCondition;
    private String checkInNotes;
    private String status;
    private Long approvedByUserId;
    private LocalDateTime approvedAt;
}

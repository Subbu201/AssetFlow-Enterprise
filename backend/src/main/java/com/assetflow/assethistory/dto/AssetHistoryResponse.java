package com.assetflow.assethistory.dto;

import com.assetflow.common.AssetStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssetHistoryResponse {
    private Long id;
    private Long assetId;
    private String action;
    private AssetStatus previousStatus;
    private AssetStatus newStatus;
    private String previousValue;
    private String newValue;
    private String remarks;
    private Long performedByUserId;
    private LocalDateTime eventTime;
}

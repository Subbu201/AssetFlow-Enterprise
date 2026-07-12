package com.assetflow.assetreturn.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssetReturnResponse {
    private Long id;
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

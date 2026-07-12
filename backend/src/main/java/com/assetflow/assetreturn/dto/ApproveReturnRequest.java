package com.assetflow.assetreturn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveReturnRequest {
    private Long approvedByUserId;
    private LocalDateTime approvedAt;
    private String checkInNotes;
}

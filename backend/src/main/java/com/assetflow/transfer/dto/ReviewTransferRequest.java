package com.assetflow.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewTransferRequest {
    private Long reviewedByUserId;
    private LocalDateTime reviewedAt;
    private String reviewComments;
    private boolean approved;
}

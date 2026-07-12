package com.assetflow.booking.dto;

import com.assetflow.common.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {

    private Long id;
    private Long assetId;
    private Long bookedByUserId;
    private Long bookedForDepartmentId;
    private String purpose;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private String cancellationReason;
    private boolean reminderSent;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

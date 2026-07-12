package com.assetflow.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateBookingRequest {

    @NotNull(message = "assetId is required")
    private Long assetId;

    /** Optional — for department-level bookings. */
    private Long bookedForDepartmentId;

    @NotBlank(message = "purpose is required")
    private String purpose;

    @NotNull(message = "startTime is required")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    private LocalDateTime endTime;
}

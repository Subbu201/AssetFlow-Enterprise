package com.assetflow.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingAvailabilityResponse {

    private Long assetId;
    private LocalDate date;

    /** Booked intervals (UPCOMING or ONGOING) for the requested date. */
    private List<BookedSlot> bookedSlots;

    @Data
    @Builder
    public static class BookedSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
    }
}

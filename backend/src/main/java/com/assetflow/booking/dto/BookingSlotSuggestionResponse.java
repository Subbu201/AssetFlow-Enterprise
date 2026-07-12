package com.assetflow.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response for the smart slot-suggestion endpoint.
 * Returns up to 5 available non-overlapping time windows.
 */
@Data
@Builder
public class BookingSlotSuggestionResponse {

    private Long assetId;
    private LocalDate date;
    private int durationMinutes;
    private List<SuggestedSlot> suggestions;

    @Data
    @Builder
    public static class SuggestedSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}

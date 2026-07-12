package com.assetflow.booking;

import com.assetflow.booking.dto.BookingSlotSuggestionResponse;
import com.assetflow.booking.entity.ResourceBooking;
import com.assetflow.booking.repository.ResourceBookingRepository;
import com.assetflow.booking.service.BookingSlotSuggestionService;
import com.assetflow.common.BookingStatus;
import com.assetflow.maintenance.service.MaintenanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the optional Smart Booking Slot Suggestion feature.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingSlotSuggestionServiceTest {

    @Mock private ResourceBookingRepository bookingRepository;
    @Mock private MaintenanceService maintenanceService;

    @InjectMocks private BookingSlotSuggestionService service;

    private static final Long ASSET_ID = 5L;
    private static final LocalDate DATE = LocalDate.now().plusDays(1);

    // =========================================================================
    // Suggestions skip occupied periods
    // =========================================================================

    @Test
    @DisplayName("Suggestions must skip occupied periods and return non-overlapping slots")
    void suggestionsSkipOccupiedPeriods() {
        // Existing booking: 09:00-10:00
        ResourceBooking existing = buildBooking(DATE, 9, 0, 10, 0);

        when(maintenanceService.isAssetUnderActiveMaintenance(ASSET_ID)).thenReturn(false);
        when(bookingRepository.findActiveBookingsForDay(eq(ASSET_ID), any(), any()))
                .thenReturn(List.of(existing));

        BookingSlotSuggestionResponse result = service.suggest(
                ASSET_ID, DATE, 60, LocalTime.of(9, 0), LocalTime.of(18, 0));

        // All suggestions must start at or after 10:00 (after the existing booking ends)
        assertThat(result.getSuggestions()).isNotEmpty();
        result.getSuggestions().forEach(slot ->
                assertThat(slot.getStartTime().toLocalTime()).isAfterOrEqualTo(LocalTime.of(10, 0)));
    }

    // =========================================================================
    // Suggestion can start exactly when another booking ends
    // =========================================================================

    @Test
    @DisplayName("Suggestion can start exactly when a previous booking ends (back-to-back allowed)")
    void suggestionStartsExactlyWhenOtherEnds() {
        // Existing: 09:00-10:00 — first suggestion should be at 10:00
        ResourceBooking existing = buildBooking(DATE, 9, 0, 10, 0);

        when(maintenanceService.isAssetUnderActiveMaintenance(ASSET_ID)).thenReturn(false);
        when(bookingRepository.findActiveBookingsForDay(eq(ASSET_ID), any(), any()))
                .thenReturn(List.of(existing));

        BookingSlotSuggestionResponse result = service.suggest(
                ASSET_ID, DATE, 60, LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertThat(result.getSuggestions()).isNotEmpty();
        // First suggestion must start at 10:00
        assertThat(result.getSuggestions().get(0).getStartTime().toLocalTime())
                .isEqualTo(LocalTime.of(10, 0));
    }

    // =========================================================================
    // No suggestions when full day is occupied
    // =========================================================================

    @Test
    @DisplayName("No suggestions returned when the full working day is occupied")
    void noSuggestionsWhenFullDayOccupied() {
        // Existing booking covers the entire working window: 09:00-18:00
        ResourceBooking fullDay = buildBooking(DATE, 9, 0, 18, 0);

        when(maintenanceService.isAssetUnderActiveMaintenance(ASSET_ID)).thenReturn(false);
        when(bookingRepository.findActiveBookingsForDay(eq(ASSET_ID), any(), any()))
                .thenReturn(List.of(fullDay));

        BookingSlotSuggestionResponse result = service.suggest(
                ASSET_ID, DATE, 60, LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertThat(result.getSuggestions()).isEmpty();
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private ResourceBooking buildBooking(LocalDate date, int startHour, int startMin,
                                          int endHour, int endMin) {
        ResourceBooking b = ResourceBooking.builder()
                .assetId(ASSET_ID)
                .bookedByUserId(99L)
                .purpose("Test")
                .startTime(date.atTime(startHour, startMin))
                .endTime(date.atTime(endHour, endMin))
                .status(BookingStatus.UPCOMING)
                .build();
        b.setId(100L);
        return b;
    }
}

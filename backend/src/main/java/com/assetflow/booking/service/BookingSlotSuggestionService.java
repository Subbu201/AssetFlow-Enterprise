package com.assetflow.booking.service;

import com.assetflow.booking.dto.BookingSlotSuggestionResponse;
import com.assetflow.booking.dto.BookingSlotSuggestionResponse.SuggestedSlot;
import com.assetflow.booking.entity.ResourceBooking;
import com.assetflow.booking.repository.ResourceBookingRepository;
import com.assetflow.maintenance.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Suggests up to 5 available non-overlapping booking slots for an asset
 * on a given date within working hours.
 *
 * Algorithm:
 *  1. Collect all UPCOMING/ONGOING bookings for the asset on the requested day.
 *  2. Walk the working window in increments of 1 minute, attempting to place
 *     a slot of durationMinutes that does not overlap any existing booking.
 *  3. After placing a slot, jump the cursor to that slot's end time (greedy).
 *  4. Stop when 5 slots are found or the working window is exhausted.
 *  5. Never return past slots.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingSlotSuggestionService {

    private static final int MAX_SUGGESTIONS = 5;

    private final ResourceBookingRepository bookingRepository;
    private final MaintenanceService maintenanceService;

    /**
     * @param assetId         target asset
     * @param date            date to suggest slots on
     * @param durationMinutes desired booking length in minutes
     * @param workingStart    earliest start of working hours (e.g. 09:00)
     * @param workingEnd      latest end of working hours (e.g. 18:00)
     * @return up to 5 suggested slots
     */
    public BookingSlotSuggestionResponse suggest(
            Long assetId,
            LocalDate date,
            int durationMinutes,
            LocalTime workingStart,
            LocalTime workingEnd) {

        // If asset is under maintenance, return empty suggestions
        if (maintenanceService.isAssetUnderActiveMaintenance(assetId)) {
            log.debug("Asset {} is under maintenance — no suggestions returned", assetId);
            return BookingSlotSuggestionResponse.builder()
                    .assetId(assetId)
                    .date(date)
                    .durationMinutes(durationMinutes)
                    .suggestions(List.of())
                    .build();
        }

        LocalDateTime windowStart = date.atTime(workingStart);
        LocalDateTime windowEnd   = date.atTime(workingEnd);
        LocalDateTime now         = LocalDateTime.now();

        // Never return past slots
        if (windowStart.isBefore(now)) {
            windowStart = now;
        }

        // Fetch existing active bookings on this day — wrap in ArrayList to allow sort
        List<ResourceBooking> existing = new java.util.ArrayList<>(bookingRepository.findActiveBookingsForDay(
                assetId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()));

        // Sort by start time
        existing.sort(Comparator.comparing(ResourceBooking::getStartTime));

        List<SuggestedSlot> suggestions = new ArrayList<>();
        LocalDateTime cursor = windowStart;


        while (suggestions.size() < MAX_SUGGESTIONS) {
            final LocalDateTime currentCursor = cursor;
            LocalDateTime candidateEnd = currentCursor.plusMinutes(durationMinutes);

            if (candidateEnd.isAfter(windowEnd)) {
                break; // no more room in the working window
            }

            final LocalDateTime finalCandidateEnd = candidateEnd;

            // Does this candidate overlap any existing booking?
            boolean overlaps = existing.stream().anyMatch(b ->
                    b.getStartTime().isBefore(finalCandidateEnd)
                    && b.getEndTime().isAfter(currentCursor));

            if (!overlaps) {
                suggestions.add(SuggestedSlot.builder()
                        .startTime(currentCursor)
                        .endTime(finalCandidateEnd)
                        .build());
                // Jump past this slot
                cursor = finalCandidateEnd;
            } else {
                // Jump cursor to the end of the conflicting booking
                ResourceBooking firstConflict = existing.stream()
                        .filter(b -> b.getStartTime().isBefore(finalCandidateEnd) && b.getEndTime().isAfter(currentCursor))
                        .min(Comparator.comparing(ResourceBooking::getStartTime))
                        .orElseThrow();
                cursor = firstConflict.getEndTime();
            }
        }


        return BookingSlotSuggestionResponse.builder()
                .assetId(assetId)
                .date(date)
                .durationMinutes(durationMinutes)
                .suggestions(suggestions)
                .build();
    }
}

package com.assetflow.booking.controller;

import com.assetflow.booking.dto.*;
import com.assetflow.booking.service.BookingSlotSuggestionService;
import com.assetflow.booking.service.ResourceBookingService;
import com.assetflow.common.ApiResponse;
import com.assetflow.common.BookingStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class ResourceBookingController {

    private final ResourceBookingService bookingService;
    private final BookingSlotSuggestionService suggestionService;
    private final com.assetflow.auth.repository.UserRepository userRepository;

    // ------------------------------------------------------------------
    // Create booking
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        String role = extractRole(userDetails);
        BookingResponse response = bookingService.createBooking(request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", response));
    }

    // ------------------------------------------------------------------
    // Get all bookings (ADMIN / ASSET_MANAGER)
    // ------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAllBookings(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) Long bookedByUserId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTo,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BookingResponse> page = bookingService.getAllBookings(
                assetId, bookedByUserId, departmentId, status, startFrom, startTo, pageable);
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved", page));
    }

    // ------------------------------------------------------------------
    // Get single booking
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved", bookingService.getById(id)));
    }

    // ------------------------------------------------------------------
    // Get bookings for a specific asset
    // ------------------------------------------------------------------

    @GetMapping("/resource/{assetId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getByAsset(
            @PathVariable Long assetId,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success("Bookings for asset retrieved",
                        bookingService.getBookingsByAsset(assetId, pageable)));
    }

    // ------------------------------------------------------------------
    // My bookings
    // ------------------------------------------------------------------

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success("Your bookings retrieved",
                        bookingService.getMyBookings(userId, pageable)));
    }

    // ------------------------------------------------------------------
    // Availability
    // ------------------------------------------------------------------

    @GetMapping("/resource/{assetId}/availability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingAvailabilityResponse>> getAvailability(
            @PathVariable Long assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(
                ApiResponse.success("Availability retrieved",
                        bookingService.getAvailability(assetId, date)));
    }

    // ------------------------------------------------------------------
    // Smart slot suggestions (add-on feature)
    // ------------------------------------------------------------------

    @GetMapping("/resource/{assetId}/suggestions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingSlotSuggestionResponse>> getSuggestions(
            @PathVariable Long assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int durationMinutes,
            @RequestParam(defaultValue = "09:00") String workingStart,
            @RequestParam(defaultValue = "18:00") String workingEnd) {

        LocalTime start = LocalTime.parse(workingStart);
        LocalTime end   = LocalTime.parse(workingEnd);

        return ResponseEntity.ok(
                ApiResponse.success("Slot suggestions retrieved",
                        suggestionService.suggest(assetId, date, durationMinutes, start, end)));
    }

    // ------------------------------------------------------------------
    // Reschedule
    // ------------------------------------------------------------------

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> reschedule(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success("Booking rescheduled",
                        bookingService.rescheduleBooking(id, request, userId)));
    }

    // ------------------------------------------------------------------
    // Cancel
    // ------------------------------------------------------------------

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        String role = extractRole(userDetails);
        if (request == null) request = new CancelBookingRequest();
        return ResponseEntity.ok(
                ApiResponse.success("Booking cancelled",
                        bookingService.cancelBooking(id, request, userId, role)));
    }

    // ------------------------------------------------------------------
    // Admin: run status update
    // ------------------------------------------------------------------

    @PostMapping("/statuses/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> runStatusUpdate() {
        int updated = bookingService.updateBookingStatuses();
        return ResponseEntity.ok(ApiResponse.success(updated + " booking(s) status updated", updated));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Long extractUserId(UserDetails userDetails) {
        if (userDetails instanceof com.assetflow.security.jwt.CustomUserDetails customUserDetails) {
            return customUserDetails.getId();
        }
        String username = userDetails.getUsername();
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            return userRepository.findByEmailIgnoreCase(username)
                    .map(com.assetflow.auth.entity.UserAccount::getId)
                    .orElse(-1L);
        }
    }

    private String extractRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("EMPLOYEE");
    }
}

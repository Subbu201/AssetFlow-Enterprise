package com.assetflow.booking;

import com.assetflow.booking.dto.*;
import com.assetflow.booking.entity.ResourceBooking;
import com.assetflow.booking.repository.ResourceBookingRepository;
import com.assetflow.booking.service.ResourceBookingService;
import com.assetflow.common.AssetStatus;
import com.assetflow.common.BookingStatus;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ForbiddenException;
import com.assetflow.maintenance.integration.AssetLifecycleGateway;
import com.assetflow.maintenance.integration.AssetSnapshot;
import com.assetflow.maintenance.service.MaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ResourceBookingService}.
 *
 * All external dependencies are mocked via Mockito — no database or Spring
 * context is required.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResourceBookingServiceTest {

    @Mock private ResourceBookingRepository bookingRepository;
    @Mock private AssetLifecycleGateway assetLifecycleGateway;
    @Mock private MaintenanceService maintenanceService;

    @InjectMocks private ResourceBookingService service;

    private static final Long ASSET_ID  = 1L;
    private static final Long USER_ID   = 10L;
    private static final Long OTHER_USER = 20L;

    /** A bookable, available asset snapshot. */
    private AssetSnapshot bookableAsset;

    @BeforeEach
    void setUp() {
        bookableAsset = AssetSnapshot.builder()
                .id(ASSET_ID)
                .status(AssetStatus.AVAILABLE)
                .bookable(true)
                .build();
    }

    // =========================================================================
    // Test 1 — 09:00-10:00 and 09:30-10:30 MUST conflict
    // =========================================================================

    @Test
    @DisplayName("1. Overlapping bookings (09:00-10:00 vs 09:30-10:30) must throw ConflictException")
    void test1_overlappingBookingsConflict() {
        // Arrange
        LocalDateTime start = today(9, 0);
        LocalDateTime end   = today(10, 0);

        ResourceBooking existing = buildBooking(1L, ASSET_ID, start, end, BookingStatus.UPCOMING);

        when(assetLifecycleGateway.getAsset(ASSET_ID)).thenReturn(bookableAsset);
        when(maintenanceService.isAssetUnderActiveMaintenance(ASSET_ID)).thenReturn(false);
        when(bookingRepository.findOverlappingBookings(eq(ASSET_ID), eq(today(9, 30)), eq(today(10, 30))))
                .thenReturn(List.of(existing));

        CreateBookingRequest req = createRequest(ASSET_ID, today(9, 30), today(10, 30));

        // Act & Assert
        assertThatThrownBy(() -> service.createBooking(req, USER_ID, "EMPLOYEE"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("conflicts");
    }

    // =========================================================================
    // Test 2 — 09:00-10:00 and 10:00-11:00 MUST be allowed
    // =========================================================================

    @Test
    @DisplayName("2. Back-to-back bookings (09:00-10:00 then 10:00-11:00) must be allowed")
    void test2_backToBackBookingsAllowed() {
        when(assetLifecycleGateway.getAsset(ASSET_ID)).thenReturn(bookableAsset);
        when(maintenanceService.isAssetUnderActiveMaintenance(ASSET_ID)).thenReturn(false);
        when(bookingRepository.findOverlappingBookings(eq(ASSET_ID), eq(today(10, 0)), eq(today(11, 0))))
                .thenReturn(List.of()); // no overlap

        ResourceBooking saved = buildBooking(2L, ASSET_ID, today(10, 0), today(11, 0), BookingStatus.UPCOMING);
        when(bookingRepository.save(any())).thenReturn(saved);

        CreateBookingRequest req = createRequest(ASSET_ID, today(10, 0), today(11, 0));
        BookingResponse response = service.createBooking(req, USER_ID, "EMPLOYEE");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BookingStatus.UPCOMING);
    }

    // =========================================================================
    // Test 3 — 09:00-10:00 and 08:00-09:00 MUST be allowed
    // =========================================================================

    @Test
    @DisplayName("3. Non-overlapping earlier booking (08:00-09:00) must be allowed")
    void test3_earlierNonOverlappingAllowed() {
        when(assetLifecycleGateway.getAsset(ASSET_ID)).thenReturn(bookableAsset);
        when(maintenanceService.isAssetUnderActiveMaintenance(ASSET_ID)).thenReturn(false);
        when(bookingRepository.findOverlappingBookings(eq(ASSET_ID), eq(today(8, 0)), eq(today(9, 0))))
                .thenReturn(List.of());

        ResourceBooking saved = buildBooking(3L, ASSET_ID, today(8, 0), today(9, 0), BookingStatus.UPCOMING);
        when(bookingRepository.save(any())).thenReturn(saved);

        CreateBookingRequest req = createRequest(ASSET_ID, today(8, 0), today(9, 0));
        BookingResponse response = service.createBooking(req, USER_ID, "EMPLOYEE");

        assertThat(response).isNotNull();
    }

    // =========================================================================
    // Test 4 — Exact same interval MUST conflict
    // =========================================================================

    @Test
    @DisplayName("4. Exactly overlapping booking interval must conflict")
    void test4_exactSameIntervalConflicts() {
        LocalDateTime start = today(9, 0);
        LocalDateTime end   = today(10, 0);

        ResourceBooking existing = buildBooking(1L, ASSET_ID, start, end, BookingStatus.UPCOMING);

        when(assetLifecycleGateway.getAsset(ASSET_ID)).thenReturn(bookableAsset);
        when(maintenanceService.isAssetUnderActiveMaintenance(ASSET_ID)).thenReturn(false);
        when(bookingRepository.findOverlappingBookings(eq(ASSET_ID), eq(start), eq(end)))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.createBooking(createRequest(ASSET_ID, start, end), USER_ID, "EMPLOYEE"))
                .isInstanceOf(ConflictException.class);
    }

    // =========================================================================
    // Test 5 — CANCELLED booking does NOT create conflict
    // =========================================================================

    @Test
    @DisplayName("5. CANCELLED booking must not block a new booking in the same slot")
    void test5_cancelledBookingDoesNotConflict() {
        // The repository query already excludes CANCELLED bookings by design.
        // We simulate that by returning an empty list when only a CANCELLED one existed.
        when(assetLifecycleGateway.getAsset(ASSET_ID)).thenReturn(bookableAsset);
        when(maintenanceService.isAssetUnderActiveMaintenance(ASSET_ID)).thenReturn(false);
        when(bookingRepository.findOverlappingBookings(eq(ASSET_ID), eq(today(9, 0)), eq(today(10, 0))))
                .thenReturn(List.of()); // CANCELLED bookings excluded by repository query

        ResourceBooking saved = buildBooking(5L, ASSET_ID, today(9, 0), today(10, 0), BookingStatus.UPCOMING);
        when(bookingRepository.save(any())).thenReturn(saved);

        assertThatNoException().isThrownBy(() ->
                service.createBooking(createRequest(ASSET_ID, today(9, 0), today(10, 0)), USER_ID, "EMPLOYEE"));
    }

    // =========================================================================
    // Test 6 — Reschedule checks overlap again
    // =========================================================================

    @Test
    @DisplayName("6. Reschedule must check overlap against other bookings")
    void test6_rescheduleChecksOverlap() {
        ResourceBooking mine = buildBooking(10L, ASSET_ID, today(9, 0), today(10, 0), BookingStatus.UPCOMING);
        mine.setBookedByUserId(USER_ID);

        ResourceBooking other = buildBooking(11L, ASSET_ID, today(10, 30), today(11, 30), BookingStatus.UPCOMING);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(mine));
        when(assetLifecycleGateway.getAsset(ASSET_ID)).thenReturn(bookableAsset);
        when(maintenanceService.isAssetUnderActiveMaintenance(ASSET_ID)).thenReturn(false);
        when(bookingRepository.findOverlappingBookingsExcluding(eq(ASSET_ID), eq(today(10, 30)), eq(today(11, 30)), eq(10L)))
                .thenReturn(List.of(other));

        RescheduleBookingRequest req = new RescheduleBookingRequest();
        req.setStartTime(today(10, 30));
        req.setEndTime(today(11, 30));

        assertThatThrownBy(() -> service.rescheduleBooking(10L, req, USER_ID))
                .isInstanceOf(ConflictException.class);
    }

    // =========================================================================
    // Test 7 — Employee cannot cancel another employee's booking
    // =========================================================================

    @Test
    @DisplayName("7. Employee cannot cancel another employee's booking")
    void test7_employeeCannotCancelOtherBooking() {
        ResourceBooking booking = buildBooking(20L, ASSET_ID, today(9, 0), today(10, 0), BookingStatus.UPCOMING);
        booking.setBookedByUserId(OTHER_USER);

        when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));

        CancelBookingRequest req = new CancelBookingRequest();
        assertThatThrownBy(() -> service.cancelBooking(20L, req, USER_ID, "EMPLOYEE"))
                .isInstanceOf(ForbiddenException.class);
    }

    // =========================================================================
    // Test 8 — Asset under maintenance cannot be booked
    // =========================================================================

    @Test
    @DisplayName("8. Asset under maintenance must not be bookable")
    void test8_assetUnderMaintenanceCannotBeBooked() {
        when(assetLifecycleGateway.getAsset(ASSET_ID)).thenReturn(bookableAsset);
        when(maintenanceService.isAssetUnderActiveMaintenance(ASSET_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.createBooking(
                createRequest(ASSET_ID, today(9, 0), today(10, 0)), USER_ID, "EMPLOYEE"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("maintenance");
    }

    // =========================================================================
    // Test 9 — Non-bookable asset cannot be booked
    // =========================================================================

    @Test
    @DisplayName("9. Non-bookable (non-shared) asset must not be bookable")
    void test9_nonBookableAssetRejected() {
        AssetSnapshot nonBookable = AssetSnapshot.builder()
                .id(ASSET_ID).status(AssetStatus.AVAILABLE).bookable(false).build();

        when(assetLifecycleGateway.getAsset(ASSET_ID)).thenReturn(nonBookable);

        assertThatThrownBy(() -> service.createBooking(
                createRequest(ASSET_ID, today(9, 0), today(10, 0)), USER_ID, "EMPLOYEE"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not a bookable");
    }

    // =========================================================================
    // Test 10 — UPCOMING → ONGOING when start time reached
    // =========================================================================

    @Test
    @DisplayName("10. UPCOMING booking transitions to ONGOING when startTime is reached")
    void test10_upcomingBecomesOngoing() {
        LocalDateTime pastStart  = LocalDateTime.now().minusHours(1);
        LocalDateTime futureEnd  = LocalDateTime.now().plusHours(1);

        ResourceBooking upcoming = buildBooking(30L, ASSET_ID, pastStart, futureEnd, BookingStatus.UPCOMING);

        when(bookingRepository.findBookingsToMarkOngoing(any())).thenReturn(List.of(upcoming));
        when(bookingRepository.findBookingsToMarkCompleted(any())).thenReturn(List.of());
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int count = service.updateBookingStatuses();

        assertThat(count).isEqualTo(1);
        assertThat(upcoming.getStatus()).isEqualTo(BookingStatus.ONGOING);
    }

    // =========================================================================
    // Test 11 — ONGOING → COMPLETED after endTime
    // =========================================================================

    @Test
    @DisplayName("11. ONGOING booking transitions to COMPLETED after endTime passes")
    void test11_ongoingBecomesCompleted() {
        LocalDateTime pastStart = LocalDateTime.now().minusHours(3);
        LocalDateTime pastEnd   = LocalDateTime.now().minusHours(1);

        ResourceBooking ongoing = buildBooking(31L, ASSET_ID, pastStart, pastEnd, BookingStatus.ONGOING);

        when(bookingRepository.findBookingsToMarkOngoing(any())).thenReturn(List.of());
        when(bookingRepository.findBookingsToMarkCompleted(any())).thenReturn(List.of(ongoing));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int count = service.updateBookingStatuses();

        assertThat(count).isEqualTo(1);
        assertThat(ongoing.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    // =========================================================================
    // Test 12 — CANCELLED status is not changed by updateBookingStatuses
    // =========================================================================

    @Test
    @DisplayName("12. CANCELLED booking status must never be changed by updateBookingStatuses")
    void test12_cancelledNotChanged() {
        // Repository queries exclude CANCELLED; simulate by returning empty lists
        when(bookingRepository.findBookingsToMarkOngoing(any())).thenReturn(List.of());
        when(bookingRepository.findBookingsToMarkCompleted(any())).thenReturn(List.of());

        int count = service.updateBookingStatuses();

        assertThat(count).isEqualTo(0);
        verify(bookingRepository, never()).save(argThat(b -> b.getStatus() == BookingStatus.CANCELLED));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Returns a LocalDateTime for today at the given hour:minute in the future. */
    private static LocalDateTime today(int hour, int minute) {
        return LocalDateTime.now().toLocalDate().atTime(hour, minute).plusDays(1);
    }

    private static CreateBookingRequest createRequest(Long assetId, LocalDateTime start, LocalDateTime end) {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setAssetId(assetId);
        req.setPurpose("Test booking");
        req.setStartTime(start);
        req.setEndTime(end);
        return req;
    }

    private static ResourceBooking buildBooking(Long id, Long assetId,
                                                 LocalDateTime start, LocalDateTime end,
                                                 BookingStatus status) {
        ResourceBooking b = ResourceBooking.builder()
                .assetId(assetId)
                .bookedByUserId(USER_ID)
                .purpose("Test")
                .startTime(start)
                .endTime(end)
                .status(status)
                .build();
        b.setId(id);
        return b;
    }
}

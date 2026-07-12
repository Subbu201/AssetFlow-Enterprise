package com.assetflow.booking.service;

import com.assetflow.booking.dto.*;
import com.assetflow.booking.entity.ResourceBooking;
import com.assetflow.booking.repository.ResourceBookingRepository;
import com.assetflow.common.AssetStatus;
import com.assetflow.common.BookingStatus;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ForbiddenException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.maintenance.integration.AssetLifecycleGateway;
import com.assetflow.maintenance.integration.AssetSnapshot;
import com.assetflow.maintenance.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceBookingService {

    private final ResourceBookingRepository bookingRepository;
    private final AssetLifecycleGateway assetLifecycleGateway;
    private final MaintenanceService maintenanceService;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Creates a new booking after validating asset eligibility and overlap rules.
     * Uses optimistic locking via @Version on the entity.
     *
     * @param request   booking details from the client
     * @param userId    authenticated user's ID (never taken from request body)
     * @param userRole  role string of the authenticated user
     * @return saved booking response
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Long userId, String userRole) {
        // Validate times
        validateTimes(request.getStartTime(), request.getEndTime());

        // Validate asset
        AssetSnapshot asset = assetLifecycleGateway.getAsset(request.getAssetId());
        validateAssetBookable(asset);

        // Check if asset is under active maintenance
        if (maintenanceService.isAssetUnderActiveMaintenance(request.getAssetId())) {
            throw new ConflictException("Asset " + request.getAssetId() + " is currently under maintenance and cannot be booked.");
        }

        // Check overlap
        List<ResourceBooking> overlaps = bookingRepository.findOverlappingBookings(
                request.getAssetId(), request.getStartTime(), request.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new ConflictException(
                    "Booking conflicts with an existing booking for asset " + request.getAssetId());
        }

        // Department booking permission: DEPARTMENT_HEAD or above
        if (request.getBookedForDepartmentId() != null) {
            if ("EMPLOYEE".equals(userRole)) {
                throw new ForbiddenException("Only Department Heads or above can create department bookings.");
            }
        }

        ResourceBooking booking = ResourceBooking.builder()
                .assetId(request.getAssetId())
                .bookedByUserId(userId)
                .bookedForDepartmentId(request.getBookedForDepartmentId())
                .purpose(request.getPurpose())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(BookingStatus.UPCOMING)
                .reminderSent(false)
                .build();

        ResourceBooking saved = bookingRepository.save(booking);
        log.info("Booking {} created for asset {} by user {}", saved.getId(), saved.getAssetId(), userId);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Reschedule
    // -------------------------------------------------------------------------

    @Transactional
    public BookingResponse rescheduleBooking(Long id, RescheduleBookingRequest request, Long userId) {
        ResourceBooking booking = findOrThrow(id);

        // Ownership check
        if (!booking.getBookedByUserId().equals(userId)) {
            throw new ForbiddenException("You may only reschedule your own bookings.");
        }

        // Only UPCOMING bookings can be rescheduled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("A CANCELLED booking cannot be rescheduled.");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ConflictException("A COMPLETED booking cannot be rescheduled.");
        }

        validateTimes(request.getStartTime(), request.getEndTime());

        // Overlap check — excluding self
        List<ResourceBooking> overlaps = bookingRepository.findOverlappingBookingsExcluding(
                booking.getAssetId(), request.getStartTime(), request.getEndTime(), id);
        if (!overlaps.isEmpty()) {
            throw new ConflictException(
                    "Rescheduled time conflicts with an existing booking for asset " + booking.getAssetId());
        }

        // Asset still valid?
        AssetSnapshot asset = assetLifecycleGateway.getAsset(booking.getAssetId());
        validateAssetBookable(asset);

        if (maintenanceService.isAssetUnderActiveMaintenance(booking.getAssetId())) {
            throw new ConflictException("Asset is currently under maintenance and cannot be rescheduled.");
        }

        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(BookingStatus.UPCOMING);

        return toResponse(bookingRepository.save(booking));
    }

    // -------------------------------------------------------------------------
    // Cancel
    // -------------------------------------------------------------------------

    @Transactional
    public BookingResponse cancelBooking(Long id, CancelBookingRequest request, Long userId, String userRole) {
        ResourceBooking booking = findOrThrow(id);

        // Permission: user can cancel own; ADMIN/ASSET_MANAGER can cancel any
        boolean isPrivileged = "ADMIN".equals(userRole) || "ASSET_MANAGER".equals(userRole);
        if (!isPrivileged && !booking.getBookedByUserId().equals(userId)) {
            throw new ForbiddenException("You may only cancel your own bookings.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking is already cancelled.");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ConflictException("A completed booking cannot be cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getCancellationReason());
        return toResponse(bookingRepository.save(booking));
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public Page<BookingResponse> getAllBookings(
            Long assetId, Long bookedByUserId, Long departmentId,
            BookingStatus status, LocalDateTime startFrom, LocalDateTime startTo,
            Pageable pageable) {

        Specification<ResourceBooking> spec = Specification.where((Specification<ResourceBooking>) null);

        if (assetId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("assetId"), assetId));
        }
        if (bookedByUserId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("bookedByUserId"), bookedByUserId));
        }
        if (departmentId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("bookedForDepartmentId"), departmentId));
        }
        if (status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        if (startFrom != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), startFrom));
        }
        if (startTo != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("startTime"), startTo));
        }

        return bookingRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public BookingResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public Page<BookingResponse> getBookingsByAsset(Long assetId, Pageable pageable) {
        return bookingRepository.findByAssetIdOrderByStartTimeAsc(assetId, pageable)
                .map(this::toResponse);
    }

    public Page<BookingResponse> getMyBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByBookedByUserIdOrderByStartTimeDesc(userId, pageable)
                .map(this::toResponse);
    }

    public BookingAvailabilityResponse getAvailability(Long assetId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<ResourceBooking> bookings = bookingRepository.findActiveBookingsForDay(assetId, dayStart, dayEnd);

        List<BookingAvailabilityResponse.BookedSlot> slots = bookings.stream()
                .map(b -> BookingAvailabilityResponse.BookedSlot.builder()
                        .startTime(b.getStartTime())
                        .endTime(b.getEndTime())
                        .status(b.getStatus().name())
                        .build())
                .toList();

        return BookingAvailabilityResponse.builder()
                .assetId(assetId)
                .date(date)
                .bookedSlots(slots)
                .build();
    }

    // -------------------------------------------------------------------------
    // Status lifecycle
    // -------------------------------------------------------------------------

    /**
     * Idempotent method that advances booking statuses based on current time.
     * Safe to call multiple times — CANCELLED bookings are never changed.
     * Member 4 (scheduler) will call this on a schedule.
     *
     * @return number of bookings whose status was updated
     */
    @Transactional
    public int updateBookingStatuses() {
        LocalDateTime now = LocalDateTime.now();
        int count = 0;

        // UPCOMING → ONGOING
        List<ResourceBooking> toOngoing = bookingRepository.findBookingsToMarkOngoing(now);
        for (ResourceBooking b : toOngoing) {
            b.setStatus(BookingStatus.ONGOING);
            bookingRepository.save(b);
            count++;
        }

        // ONGOING → COMPLETED
        List<ResourceBooking> toCompleted = bookingRepository.findBookingsToMarkCompleted(now);
        for (ResourceBooking b : toCompleted) {
            b.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(b);
            count++;
        }

        log.debug("updateBookingStatuses: {} bookings updated", count);
        return count;
    }

    // -------------------------------------------------------------------------
    // Cross-module utility
    // -------------------------------------------------------------------------

    /**
     * Returns true if the asset has any UPCOMING or ONGOING booking that
     * overlaps with the given interval. Used by the maintenance module to
     * block approval when active bookings exist.
     */
    public boolean hasActiveBooking(Long assetId, LocalDateTime start, LocalDateTime end) {
        return !bookingRepository.findOverlappingBookings(assetId, start, end).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResourceBooking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("startTime and endTime are required.");
        }
        if (!start.isBefore(end)) {
            throw new BadRequestException("startTime must be before endTime.");
        }
        if (end.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Booking must not be entirely in the past.");
        }
    }

    private void validateAssetBookable(AssetSnapshot asset) {
        if (!asset.isBookable()) {
            throw new BadRequestException("Asset " + asset.getId() + " is not a bookable/shared asset.");
        }
        AssetStatus status = asset.getStatus();
        if (status == AssetStatus.UNDER_MAINTENANCE) {
            throw new ConflictException("Asset " + asset.getId() + " is currently under maintenance.");
        }
        if (status == AssetStatus.LOST || status == AssetStatus.RETIRED || status == AssetStatus.DISPOSED) {
            throw new BadRequestException("Asset " + asset.getId() + " cannot be booked (status: " + status + ").");
        }
    }

    private BookingResponse toResponse(ResourceBooking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .assetId(b.getAssetId())
                .bookedByUserId(b.getBookedByUserId())
                .bookedForDepartmentId(b.getBookedForDepartmentId())
                .purpose(b.getPurpose())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .status(b.getStatus())
                .cancellationReason(b.getCancellationReason())
                .reminderSent(b.isReminderSent())
                .version(b.getVersion())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}

package com.assetflow.booking.repository;

import com.assetflow.booking.entity.ResourceBooking;
import com.assetflow.common.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ResourceBookingRepository
        extends JpaRepository<ResourceBooking, Long>, JpaSpecificationExecutor<ResourceBooking> {

    /**
     * Finds all non-cancelled bookings for an asset whose time window overlaps
     * with [requestedStart, requestedEnd).
     *
     * Overlap rule:  existing.startTime < requestedEnd
     *            AND existing.endTime   > requestedStart
     *
     * Example — conflict:   existing 09:00-10:00, request 09:30-10:30  → overlap
     * Example — allowed:    existing 09:00-10:00, request 10:00-11:00  → no overlap
     */
    @Query("""
            SELECT rb FROM ResourceBooking rb
            WHERE rb.assetId = :assetId
              AND rb.status NOT IN (com.assetflow.common.BookingStatus.CANCELLED)
              AND rb.startTime < :endTime
              AND rb.endTime   > :startTime
            """)
    List<ResourceBooking> findOverlappingBookings(
            @Param("assetId") Long assetId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Same as above but excludes a specific booking ID (used during reschedule
     * to avoid self-conflict).
     */
    @Query("""
            SELECT rb FROM ResourceBooking rb
            WHERE rb.assetId = :assetId
              AND rb.id <> :excludeId
              AND rb.status NOT IN (com.assetflow.common.BookingStatus.CANCELLED)
              AND rb.startTime < :endTime
              AND rb.endTime   > :startTime
            """)
    List<ResourceBooking> findOverlappingBookingsExcluding(
            @Param("assetId") Long assetId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId);

    /**
     * Returns all active (non-terminal) bookings for a given asset used for
     * maintenance cross-check.
     */
    @Query("""
            SELECT rb FROM ResourceBooking rb
            WHERE rb.assetId = :assetId
              AND rb.status IN (:statuses)
              AND rb.endTime > :now
            """)
    List<ResourceBooking> findActiveBookingsForAsset(
            @Param("assetId") Long assetId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("now") LocalDateTime now);

    /** Bookings that should transition from UPCOMING → ONGOING. */
    @Query("""
            SELECT rb FROM ResourceBooking rb
            WHERE rb.status = com.assetflow.common.BookingStatus.UPCOMING
              AND rb.startTime <= :now
            """)
    List<ResourceBooking> findBookingsToMarkOngoing(@Param("now") LocalDateTime now);

    /** Bookings that should transition from ONGOING → COMPLETED. */
    @Query("""
            SELECT rb FROM ResourceBooking rb
            WHERE rb.status = com.assetflow.common.BookingStatus.ONGOING
              AND rb.endTime <= :now
            """)
    List<ResourceBooking> findBookingsToMarkCompleted(@Param("now") LocalDateTime now);

    /** All bookings for an asset ordered by start time ascending. */
    Page<ResourceBooking> findByAssetIdOrderByStartTimeAsc(Long assetId, Pageable pageable);

    /** All bookings by a user ordered by start time descending. */
    Page<ResourceBooking> findByBookedByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);

    /** Bookings for an asset whose time window touches the given day — used for availability. */
    @Query("""
            SELECT rb FROM ResourceBooking rb
            WHERE rb.assetId = :assetId
              AND rb.status IN (com.assetflow.common.BookingStatus.UPCOMING,
                               com.assetflow.common.BookingStatus.ONGOING)
              AND rb.startTime < :dayEnd
              AND rb.endTime   > :dayStart
            ORDER BY rb.startTime ASC
            """)
    List<ResourceBooking> findActiveBookingsForDay(
            @Param("assetId") Long assetId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd);
}

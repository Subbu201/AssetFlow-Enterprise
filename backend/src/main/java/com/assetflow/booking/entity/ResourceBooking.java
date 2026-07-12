package com.assetflow.booking.entity;

import com.assetflow.common.BaseEntity;
import com.assetflow.common.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a time-bound reservation of a shared/bookable asset.
 * Uses optimistic locking via {@code @Version} to prevent double-booking
 * under concurrent requests.
 */
@Entity
@Table(name = "resource_bookings",
        indexes = {
            @Index(name = "idx_booking_asset_id", columnList = "assetId"),
            @Index(name = "idx_booking_user_id", columnList = "bookedByUserId"),
            @Index(name = "idx_booking_status", columnList = "status"),
            @Index(name = "idx_booking_times", columnList = "startTime,endTime")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceBooking extends BaseEntity {

    /** FK to the asset being booked — stored as ID only, no JPA relation. */
    @Column(nullable = false)
    private Long assetId;

    /** FK to the user who made the booking. */
    @Column(nullable = false)
    private Long bookedByUserId;

    /** Optional: booking made on behalf of a department. */
    @Column
    private Long bookedForDepartmentId;

    @Column(nullable = false, length = 1000)
    private String purpose;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.UPCOMING;

    @Column(length = 1000)
    private String cancellationReason;

    @Column(nullable = false)
    @Builder.Default
    private boolean reminderSent = false;

    /** Optimistic locking version field. */
    @Version
    private Long version;
}

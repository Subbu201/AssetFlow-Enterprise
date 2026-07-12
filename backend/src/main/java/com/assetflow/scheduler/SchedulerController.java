package com.assetflow.scheduler;

import com.assetflow.common.ApiResponse;
import com.assetflow.exception.ForbiddenException;
import com.assetflow.common.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/jobs")
public class SchedulerController {

    private final SchedulerService schedulerService;

    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/overdue/run")
    public ResponseEntity<ApiResponse<Long>> runOverdueAllocations() {
        requireAdmin();
        long processed = schedulerService.runOverdueAllocations();
        return ResponseEntity.ok(ApiResponse.success("Overdue allocation job completed", processed));
    }

    @PostMapping("/booking-status/run")
    public ResponseEntity<ApiResponse<Long>> runBookingStatusUpdate() {
        requireAdmin();
        long processed = schedulerService.runBookingStatusUpdate();
        return ResponseEntity.ok(ApiResponse.success("Booking status job completed", processed));
    }

    @PostMapping("/booking-reminder/run")
    public ResponseEntity<ApiResponse<Long>> runBookingReminders() {
        requireAdmin();
        long processed = schedulerService.runBookingReminders();
        return ResponseEntity.ok(ApiResponse.success("Booking reminder job completed", processed));
    }

    @PostMapping("/maintenance-reminder/run")
    public ResponseEntity<ApiResponse<Long>> runMaintenanceReminders() {
        requireAdmin();
        long processed = schedulerService.runMaintenanceReminders();
        return ResponseEntity.ok(ApiResponse.success("Maintenance reminder job completed", processed));
    }

    private void requireAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name()));
        if (!isAdmin) {
            throw new ForbiddenException("Access denied");
        }
    }
}

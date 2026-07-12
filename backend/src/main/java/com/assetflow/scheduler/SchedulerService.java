package com.assetflow.scheduler;

import com.assetflow.activitylog.ActivityLogService;
import com.assetflow.scheduler.gateway.AllocationSchedulerGateway;
import com.assetflow.scheduler.gateway.BookingSchedulerGateway;
import com.assetflow.scheduler.gateway.MaintenanceSchedulerGateway;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

    private final AllocationSchedulerGateway allocationGateway;
    private final BookingSchedulerGateway bookingGateway;
    private final MaintenanceSchedulerGateway maintenanceGateway;
    private final ActivityLogService activityLogService;

    public SchedulerService(AllocationSchedulerGateway allocationGateway,
                            BookingSchedulerGateway bookingGateway,
                            MaintenanceSchedulerGateway maintenanceGateway,
                            ActivityLogService activityLogService) {
        this.allocationGateway = allocationGateway;
        this.bookingGateway = bookingGateway;
        this.maintenanceGateway = maintenanceGateway;
        this.activityLogService = activityLogService;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void overdueAllocationCheck() {
        int count = allocationGateway.markOverdueAllocations();
        activityLogService.log(null, "OVERDUE_ALLOCATION_CHECK", "SCHEDULER", null, "Processed " + count + " overdue allocations");
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void bookingStatusUpdate() {
        int count = bookingGateway.updateBookingStatuses();
        activityLogService.log(null, "BOOKING_STATUS_UPDATE", "SCHEDULER", null, "Processed " + count + " booking status records");
    }

    @Scheduled(cron = "0 30 2 * * ?")
    public void bookingReminderCheck() {
        int count = bookingGateway.sendBookingReminders();
        activityLogService.log(null, "BOOKING_REMINDER_CHECK", "SCHEDULER", null, "Processed " + count + " booking reminders");
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void maintenanceReminderCheck() {
        int count = maintenanceGateway.processMaintenanceReminders();
        activityLogService.log(null, "MAINTENANCE_REMINDER_CHECK", "SCHEDULER", null, "Processed " + count + " maintenance reminders");
    }

    public int runOverdueAllocations() {
        return allocationGateway.markOverdueAllocations();
    }

    public int runBookingStatusUpdate() {
        return bookingGateway.updateBookingStatuses();
    }

    public int runBookingReminders() {
        return bookingGateway.sendBookingReminders();
    }

    public int runMaintenanceReminders() {
        return maintenanceGateway.processMaintenanceReminders();
    }
}

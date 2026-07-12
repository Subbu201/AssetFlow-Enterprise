package com.assetflow.scheduler;

import com.assetflow.scheduler.gateway.AllocationSchedulerGateway;
import com.assetflow.scheduler.gateway.BookingSchedulerGateway;
import com.assetflow.scheduler.gateway.MaintenanceSchedulerGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private static final Duration MIN_RUN_INTERVAL = Duration.ofHours(23);
    private static final Map<String, String> JOB_CRON_EXPRESSIONS = createJobCronExpressions();

    private final AllocationSchedulerGateway allocationGateway;
    private final BookingSchedulerGateway bookingGateway;
    private final MaintenanceSchedulerGateway maintenanceGateway;
    private final Map<String, LocalDateTime> lastRunTimes = new ConcurrentHashMap<>();

    public SchedulerService(AllocationSchedulerGateway allocationGateway,
                            BookingSchedulerGateway bookingGateway,
                            MaintenanceSchedulerGateway maintenanceGateway) {
        this.allocationGateway = allocationGateway;
        this.bookingGateway = bookingGateway;
        this.maintenanceGateway = maintenanceGateway;
    }

    private static Map<String, String> createJobCronExpressions() {
        Map<String, String> expressions = new LinkedHashMap<>();
        expressions.put("OVERDUE_ALLOCATION_CHECK", "0 0 1 * * ?");
        expressions.put("BOOKING_STATUS_UPDATE", "0 0 2 * * ?");
        expressions.put("BOOKING_REMINDER_CHECK", "0 30 2 * * ?");
        expressions.put("MAINTENANCE_REMINDER_CHECK", "0 0 3 * * ?");
        return Collections.unmodifiableMap(expressions);
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void overdueAllocationCheck() {
        runScheduledJob("OVERDUE_ALLOCATION_CHECK", () -> allocationGateway.markOverdueAllocations());
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void bookingStatusUpdate() {
        runScheduledJob("BOOKING_STATUS_UPDATE", () -> bookingGateway.updateBookingStatuses());
    }

    @Scheduled(cron = "0 30 2 * * ?")
    public void bookingReminderCheck() {
        runScheduledJob("BOOKING_REMINDER_CHECK", () -> bookingGateway.sendBookingReminders());
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void maintenanceReminderCheck() {
        runScheduledJob("MAINTENANCE_REMINDER_CHECK", () -> maintenanceGateway.processMaintenanceReminders());
    }

    public int runOverdueAllocations() {
        return runManualJob("OVERDUE_ALLOCATION_CHECK", () -> allocationGateway.markOverdueAllocations());
    }

    public int runBookingStatusUpdate() {
        return runManualJob("BOOKING_STATUS_UPDATE", () -> bookingGateway.updateBookingStatuses());
    }

    public int runBookingReminders() {
        return runManualJob("BOOKING_REMINDER_CHECK", () -> bookingGateway.sendBookingReminders());
    }

    public int runMaintenanceReminders() {
        return runManualJob("MAINTENANCE_REMINDER_CHECK", () -> maintenanceGateway.processMaintenanceReminders());
    }

    public Map<String, String> getJobSchedules() {
        return JOB_CRON_EXPRESSIONS;
    }

    private int runScheduledJob(String jobName, Supplier<Integer> jobAction) {
        if (!shouldExecuteJob(jobName)) {
            logger.info("Skipping scheduled {} because it already ran within {}", jobName, MIN_RUN_INTERVAL);
            return 0;
        }
        return executeJob(jobName, jobAction);
    }

    private int runManualJob(String jobName, Supplier<Integer> jobAction) {
        logger.info("Starting manual job {}", jobName);
        return executeJob(jobName, jobAction);
    }

    private int executeJob(String jobName, Supplier<Integer> jobAction) {
        try {
            int processed = jobAction.get();
            logger.info("{} completed, processed {} items", jobName, processed);
            recordRun(jobName);
            return processed;
        } catch (Exception ex) {
            logger.error("{} failed", jobName, ex);
            return 0;
        }
    }

    private boolean shouldExecuteJob(String jobName) {
        LocalDateTime lastRun = lastRunTimes.get(jobName);
        return lastRun == null || Duration.between(lastRun, LocalDateTime.now()).compareTo(MIN_RUN_INTERVAL) >= 0;
    }

    private void recordRun(String jobName) {
        lastRunTimes.put(jobName, LocalDateTime.now());
    }
}

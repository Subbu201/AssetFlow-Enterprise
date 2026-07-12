package com.assetflow.scheduler.gateway;

public interface BookingSchedulerGateway {
    int updateBookingStatuses();
    int sendBookingReminders();
}

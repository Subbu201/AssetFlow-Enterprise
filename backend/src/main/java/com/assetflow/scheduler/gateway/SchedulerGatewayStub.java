package com.assetflow.scheduler.gateway;

import org.springframework.stereotype.Component;

@Component
public class SchedulerGatewayStub implements AllocationSchedulerGateway, BookingSchedulerGateway, MaintenanceSchedulerGateway {

    @Override
    public int markOverdueAllocations() {
        return 0;
    }

    @Override
    public int updateBookingStatuses() {
        return 0;
    }

    @Override
    public int sendBookingReminders() {
        return 0;
    }

    @Override
    public int processMaintenanceReminders() {
        return 0;
    }
}

package com.assetflow.scheduler;

import com.assetflow.scheduler.gateway.AllocationSchedulerGateway;
import com.assetflow.scheduler.gateway.BookingSchedulerGateway;
import com.assetflow.scheduler.gateway.MaintenanceSchedulerGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private AllocationSchedulerGateway allocationGateway;

    @Mock
    private BookingSchedulerGateway bookingGateway;

    @Mock
    private MaintenanceSchedulerGateway maintenanceGateway;

    private SchedulerService service;

    @BeforeEach
    void setUp() {
        service = new SchedulerService(allocationGateway, bookingGateway, maintenanceGateway);
    }

    @Test
    void getJobSchedulesReturnsConfiguredCronExpressions() {
        Map<String, String> schedules = service.getJobSchedules();
        assertEquals(4, schedules.size());
        assertTrue(schedules.containsKey("OVERDUE_ALLOCATION_CHECK"));
        assertEquals("0 0 1 * * ?", schedules.get("OVERDUE_ALLOCATION_CHECK"));
    }

    @Test
    void runOverdueAllocationsReturnsZeroWhenGatewayReturnsZero() {
        when(allocationGateway.markOverdueAllocations()).thenReturn(0);
        assertEquals(0, service.runOverdueAllocations());
    }

    @Test
    void runBookingStatusUpdateReturnsGatewayCount() {
        when(bookingGateway.updateBookingStatuses()).thenReturn(7);
        assertEquals(7, service.runBookingStatusUpdate());
    }

    @Test
    void runBookingRemindersReturnsGatewayCount() {
        when(bookingGateway.sendBookingReminders()).thenReturn(5);
        assertEquals(5, service.runBookingReminders());
    }

    @Test
    void runMaintenanceRemindersReturnsGatewayCount() {
        when(maintenanceGateway.processMaintenanceReminders()).thenReturn(3);
        assertEquals(3, service.runMaintenanceReminders());
    }
}

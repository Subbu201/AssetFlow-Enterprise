package com.assetflow.report;

import com.assetflow.exception.ForbiddenException;
import com.assetflow.report.dto.AuditDiscrepancyReport;
import com.assetflow.report.dto.AssetStatusReport;
import com.assetflow.report.dto.DepartmentAllocationReport;
import com.assetflow.report.dto.MaintenanceFrequencyReport;
import com.assetflow.report.dto.OverdueAllocationReport;
import com.assetflow.report.dto.ResourceUtilizationReport;
import com.assetflow.report.gateway.AllocationReportGateway;
import com.assetflow.report.gateway.AssetReportGateway;
import com.assetflow.report.gateway.BookingReportGateway;
import com.assetflow.report.gateway.MaintenanceReportGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private AssetReportGateway assetReportGateway;

    @Mock
    private AllocationReportGateway allocationReportGateway;

    @Mock
    private BookingReportGateway bookingReportGateway;

    @Mock
    private MaintenanceReportGateway maintenanceReportGateway;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(assetReportGateway, allocationReportGateway, bookingReportGateway, maintenanceReportGateway);
    }

    @Test
    void getDepartmentAllocationReportReturnsEmptyListIfNull() {
        when(assetReportGateway.departmentAllocationSummary()).thenReturn(null);
        authenticateAsAdmin();

        assertNotNull(service.getDepartmentAllocationReport());
        assertTrue(service.getDepartmentAllocationReport().isEmpty());
    }

    @Test
    void getAssetStatusReportThrowsWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(ForbiddenException.class, () -> service.getAssetStatusReport());
    }

    @Test
    void getAuditDiscrepancyReportReturnsDataWhenAuthorized() {
        when(maintenanceReportGateway.auditDiscrepancyReport()).thenReturn(List.of(new AuditDiscrepancyReport(1L, 2L, 3L, 4L, "TYPE", "OPEN")));
        authenticateAsAssetManager();

        var result = service.getAuditDiscrepancyReport();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    private void authenticateAsAdmin() {
        var auth = new TestingAuthenticationToken("admin", "password", "ROLE_ADMIN");
        auth.setAuthenticated(true);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    private void authenticateAsAssetManager() {
        var auth = new TestingAuthenticationToken("manager", "password", "ROLE_ASSET_MANAGER");
        auth.setAuthenticated(true);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
}

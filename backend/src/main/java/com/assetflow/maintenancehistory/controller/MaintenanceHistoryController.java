package com.assetflow.maintenancehistory.controller;

import com.assetflow.common.ApiResponse;
import com.assetflow.maintenancehistory.dto.MaintenanceHistoryResponse;
import com.assetflow.maintenancehistory.service.MaintenanceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceHistoryController {

    private final MaintenanceHistoryService historyService;

    /**
     * GET /api/maintenance/{maintenanceId}/history
     *
     * Returns audit history for a maintenance request, newest first.
     * Accessible by authenticated users (ADMIN, ASSET_MANAGER see all;
     * employees see their own via the service layer if needed).
     */
    @GetMapping("/{maintenanceId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MaintenanceHistoryResponse>>> getHistory(
            @PathVariable Long maintenanceId,
            @PageableDefault(size = 20, sort = "eventTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<MaintenanceHistoryResponse> history = historyService.getHistory(maintenanceId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Maintenance history retrieved", history));
    }
}

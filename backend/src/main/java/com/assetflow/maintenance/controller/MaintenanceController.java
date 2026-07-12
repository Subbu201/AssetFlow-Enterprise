package com.assetflow.maintenance.controller;

import com.assetflow.common.ApiResponse;
import com.assetflow.common.MaintenanceStatus;
import com.assetflow.maintenance.dto.*;
import com.assetflow.maintenance.service.MaintenanceService;
import com.assetflow.maintenance.storage.MaintenanceAttachmentStorage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final MaintenanceAttachmentStorage attachmentStorage;

    // ------------------------------------------------------------------
    // Create maintenance request
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> create(
            @Valid @RequestBody CreateMaintenanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        MaintenanceResponse response = maintenanceService.createRequest(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Maintenance request created", response));
    }

    // ------------------------------------------------------------------
    // Get all requests (ADMIN / ASSET_MANAGER)
    // ------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<Page<MaintenanceResponse>>> getAll(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) Long raisedByUserId,
            @RequestParam(required = false) MaintenanceStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success("Maintenance requests retrieved",
                maintenanceService.getAllRequests(assetId, raisedByUserId, status, pageable)));
    }

    // ------------------------------------------------------------------
    // Get single request
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Maintenance request retrieved",
                maintenanceService.getById(id)));
    }

    // ------------------------------------------------------------------
    // Get requests for an asset
    // ------------------------------------------------------------------

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<Page<MaintenanceResponse>>> getByAsset(
            @PathVariable Long assetId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success("Maintenance requests for asset retrieved",
                maintenanceService.getByAsset(assetId, pageable)));
    }

    // ------------------------------------------------------------------
    // My maintenance requests
    // ------------------------------------------------------------------

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MaintenanceResponse>>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Your maintenance requests retrieved",
                maintenanceService.getMyRequests(userId, pageable)));
    }

    // ------------------------------------------------------------------
    // Pending requests
    // ------------------------------------------------------------------

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<Page<MaintenanceResponse>>> getPending(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success("Pending maintenance requests retrieved",
                maintenanceService.getPendingRequests(pageable)));
    }

    // ------------------------------------------------------------------
    // Approve
    // ------------------------------------------------------------------

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ApproveMaintenanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (request == null) request = new ApproveMaintenanceRequest();
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Maintenance request approved",
                maintenanceService.approveRequest(id, request, userId)));
    }

    // ------------------------------------------------------------------
    // Reject
    // ------------------------------------------------------------------

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectMaintenanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Maintenance request rejected",
                maintenanceService.rejectRequest(id, request, userId)));
    }

    // ------------------------------------------------------------------
    // Assign technician
    // ------------------------------------------------------------------

    @PostMapping("/{id}/assign-technician")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> assignTechnician(
            @PathVariable Long id,
            @Valid @RequestBody AssignTechnicianRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Technician assigned",
                maintenanceService.assignTechnician(id, request, userId)));
    }

    // ------------------------------------------------------------------
    // Start repair
    // ------------------------------------------------------------------

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> startRepair(
            @PathVariable Long id,
            @RequestBody(required = false) StartRepairRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (request == null) request = new StartRepairRequest();
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Repair started",
                maintenanceService.startRepair(id, request, userId)));
    }

    // ------------------------------------------------------------------
    // Resolve
    // ------------------------------------------------------------------

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> resolve(
            @PathVariable Long id,
            @Valid @RequestBody ResolveMaintenanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Maintenance request resolved",
                maintenanceService.resolveRequest(id, request, userId)));
    }

    // ------------------------------------------------------------------
    // Upload attachment
    // ------------------------------------------------------------------

    @PostMapping(value = "/{id}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        String relativePath = attachmentStorage.store(file);
        MaintenanceResponse response = maintenanceService.setAttachment(id, relativePath, userId);
        return ResponseEntity.ok(ApiResponse.success("Attachment uploaded", response));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Long extractUserId(UserDetails userDetails) {
        try {
            return Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }
}

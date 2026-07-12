package com.assetflow.maintenance.service;

import com.assetflow.common.MaintenanceStatus;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ForbiddenException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.maintenance.dto.*;
import com.assetflow.maintenance.entity.MaintenanceRequest;
import com.assetflow.maintenance.integration.AssetLifecycleGateway;
import com.assetflow.maintenance.repository.MaintenanceRequestRepository;
import com.assetflow.maintenancehistory.service.MaintenanceHistoryService;
import com.assetflow.technician.TechnicianAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    /** Active statuses — more than one of these at the same time is blocked. */
    static final List<MaintenanceStatus> ACTIVE_STATUSES = List.of(
            MaintenanceStatus.PENDING,
            MaintenanceStatus.APPROVED,
            MaintenanceStatus.TECHNICIAN_ASSIGNED,
            MaintenanceStatus.IN_PROGRESS
    );

    private final MaintenanceRequestRepository requestRepository;
    private final AssetLifecycleGateway assetLifecycleGateway;
    private final MaintenanceHistoryService historyService;
    private final TechnicianAssignmentService technicianAssignmentService;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Transactional
    public MaintenanceResponse createRequest(CreateMaintenanceRequest dto, Long raisedByUserId) {
        // Rule 6: no duplicate active request for the same asset
        if (requestRepository.existsActiveRequestForAsset(dto.getAssetId(), ACTIVE_STATUSES)) {
            throw new ConflictException(
                    "An active maintenance request already exists for asset " + dto.getAssetId()
                    + ". Resolve it before raising a new one.");
        }

        String requestNumber = generateNextRequestNumber();

        MaintenanceRequest req = MaintenanceRequest.builder()
                .requestNumber(requestNumber)
                .assetId(dto.getAssetId())
                .raisedByUserId(raisedByUserId)
                .issueDescription(dto.getIssueDescription())
                .priority(dto.getPriority())
                .attachmentUrl(dto.getAttachmentUrl())
                .status(MaintenanceStatus.PENDING)
                .build();

        MaintenanceRequest saved = requestRepository.save(req);

        // Record history: null → PENDING
        historyService.recordEvent(
                saved.getId(), saved.getAssetId(),
                null, MaintenanceStatus.PENDING,
                raisedByUserId, "Maintenance request created");

        log.info("Maintenance request {} created for asset {} by user {}",
                saved.getRequestNumber(), saved.getAssetId(), raisedByUserId);
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Approve
    // -------------------------------------------------------------------------

    @Transactional
    public MaintenanceResponse approveRequest(Long id, ApproveMaintenanceRequest dto, Long approvedByUserId) {
        MaintenanceRequest req = findOrThrow(id);

        requireStatus(req, MaintenanceStatus.PENDING,
                "Only PENDING requests can be approved. Current status: " + req.getStatus());

        MaintenanceStatus prev = req.getStatus();
        req.setStatus(MaintenanceStatus.APPROVED);
        req.setApprovedByUserId(approvedByUserId);
        req.setApprovedAt(LocalDateTime.now());

        // Rule 11: change asset status to UNDER_MAINTENANCE
        assetLifecycleGateway.markUnderMaintenance(
                req.getAssetId(), approvedByUserId,
                "Maintenance request " + req.getRequestNumber() + " approved");

        MaintenanceRequest saved = requestRepository.save(req);
        historyService.recordEvent(saved.getId(), saved.getAssetId(),
                prev, MaintenanceStatus.APPROVED, approvedByUserId, dto.getComments());

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Reject
    // -------------------------------------------------------------------------

    @Transactional
    public MaintenanceResponse rejectRequest(Long id, RejectMaintenanceRequest dto, Long rejectedByUserId) {
        MaintenanceRequest req = findOrThrow(id);

        requireStatus(req, MaintenanceStatus.PENDING,
                "Only PENDING requests can be rejected. Current status: " + req.getStatus());

        MaintenanceStatus prev = req.getStatus();
        req.setStatus(MaintenanceStatus.REJECTED);
        req.setRejectionReason(dto.getRejectionReason());

        // Rule 10: rejection must NOT change asset status

        MaintenanceRequest saved = requestRepository.save(req);
        historyService.recordEvent(saved.getId(), saved.getAssetId(),
                prev, MaintenanceStatus.REJECTED, rejectedByUserId, dto.getRejectionReason());

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Assign technician
    // -------------------------------------------------------------------------

    @Transactional
    public MaintenanceResponse assignTechnician(Long id, AssignTechnicianRequest dto, Long assignedByUserId) {
        MaintenanceRequest req = findOrThrow(id);

        // Delegate all validation to TechnicianAssignmentService
        technicianAssignmentService.validateAndAssign(req, dto.getTechnicianUserId());

        MaintenanceStatus prev = req.getStatus();
        req.setStatus(MaintenanceStatus.TECHNICIAN_ASSIGNED);
        req.setTechnicianUserId(dto.getTechnicianUserId());
        req.setTechnicianAssignedAt(LocalDateTime.now());

        MaintenanceRequest saved = requestRepository.save(req);
        historyService.recordEvent(saved.getId(), saved.getAssetId(),
                prev, MaintenanceStatus.TECHNICIAN_ASSIGNED,
                assignedByUserId, dto.getComments());

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Start repair
    // -------------------------------------------------------------------------

    @Transactional
    public MaintenanceResponse startRepair(Long id, StartRepairRequest dto, Long startedByUserId) {
        MaintenanceRequest req = findOrThrow(id);

        requireStatus(req, MaintenanceStatus.TECHNICIAN_ASSIGNED,
                "Repair can only be started after a technician is assigned. Current status: " + req.getStatus());

        MaintenanceStatus prev = req.getStatus();
        req.setStatus(MaintenanceStatus.IN_PROGRESS);
        req.setRepairStartedAt(LocalDateTime.now());

        MaintenanceRequest saved = requestRepository.save(req);
        historyService.recordEvent(saved.getId(), saved.getAssetId(),
                prev, MaintenanceStatus.IN_PROGRESS, startedByUserId, dto.getComments());

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Resolve
    // -------------------------------------------------------------------------

    @Transactional
    public MaintenanceResponse resolveRequest(Long id, ResolveMaintenanceRequest dto, Long resolvedByUserId) {
        MaintenanceRequest req = findOrThrow(id);

        requireStatus(req, MaintenanceStatus.IN_PROGRESS,
                "Request can only be resolved when IN_PROGRESS. Current status: " + req.getStatus());

        MaintenanceStatus prev = req.getStatus();
        req.setStatus(MaintenanceStatus.RESOLVED);
        req.setResolvedAt(LocalDateTime.now());
        req.setResolutionNotes(dto.getResolutionNotes());

        // Rule 15: change asset status back to AVAILABLE
        assetLifecycleGateway.markAvailable(
                req.getAssetId(), resolvedByUserId,
                "Maintenance request " + req.getRequestNumber() + " resolved");

        MaintenanceRequest saved = requestRepository.save(req);
        historyService.recordEvent(saved.getId(), saved.getAssetId(),
                prev, MaintenanceStatus.RESOLVED, resolvedByUserId, dto.getResolutionNotes());

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Attachment
    // -------------------------------------------------------------------------

    @Transactional
    public MaintenanceResponse setAttachment(Long id, String relativePath, Long userId) {
        MaintenanceRequest req = findOrThrow(id);
        req.setAttachmentUrl(relativePath);
        return toResponse(requestRepository.save(req));
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public Page<MaintenanceResponse> getAllRequests(
            Long assetId, Long raisedByUserId, MaintenanceStatus status, Pageable pageable) {

        Specification<MaintenanceRequest> spec = Specification.where((Specification<MaintenanceRequest>) null);
        if (assetId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("assetId"), assetId));
        }
        if (raisedByUserId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("raisedByUserId"), raisedByUserId));
        }
        if (status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        return requestRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public MaintenanceResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public Page<MaintenanceResponse> getMyRequests(Long userId, Pageable pageable) {
        return requestRepository.findByRaisedByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public Page<MaintenanceResponse> getByAsset(Long assetId, Pageable pageable) {
        return requestRepository.findByAssetIdOrderByCreatedAtDesc(assetId, pageable)
                .map(this::toResponse);
    }

    public Page<MaintenanceResponse> getPendingRequests(Pageable pageable) {
        return requestRepository.findPending(pageable).map(this::toResponse);
    }

    // -------------------------------------------------------------------------
    // Cross-module utility
    // -------------------------------------------------------------------------

    /**
     * Returns true when the asset has any active (non-terminal) maintenance request.
     * Used by the booking module to block bookings for assets under maintenance.
     * Used by Member 2 / integration leader to block allocation.
     */
    public boolean isAssetUnderActiveMaintenance(Long assetId) {
        return requestRepository.existsActiveRequestForAsset(assetId, ACTIVE_STATUSES);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private MaintenanceRequest findOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found: " + id));
    }

    /**
     * Asserts the request is currently in the expected status.
     * Throws {@link ConflictException} (HTTP 409) for invalid state transitions.
     */
    private void requireStatus(MaintenanceRequest req, MaintenanceStatus expected, String message) {
        if (req.getStatus() != expected) {
            throw new ConflictException(message);
        }
    }

    /**
     * Generates the next request number safely.
     * Uses MAX() on the numeric suffix rather than COUNT()+1 to avoid
     * race conditions and gaps caused by rollbacks.
     *
     * Example: existing max suffix = 3  →  returns "MR-0004"
     */
    private String generateNextRequestNumber() {
        int nextSuffix = requestRepository.findMaxRequestNumberSuffix()
                .map(max -> max + 1)
                .orElse(1);
        return String.format("MR-%04d", nextSuffix);
    }

    private MaintenanceResponse toResponse(MaintenanceRequest r) {
        return MaintenanceResponse.builder()
                .id(r.getId())
                .requestNumber(r.getRequestNumber())
                .assetId(r.getAssetId())
                .raisedByUserId(r.getRaisedByUserId())
                .issueDescription(r.getIssueDescription())
                .priority(r.getPriority())
                .attachmentUrl(r.getAttachmentUrl())
                .status(r.getStatus())
                .approvedByUserId(r.getApprovedByUserId())
                .approvedAt(r.getApprovedAt())
                .rejectionReason(r.getRejectionReason())
                .technicianUserId(r.getTechnicianUserId())
                .technicianAssignedAt(r.getTechnicianAssignedAt())
                .repairStartedAt(r.getRepairStartedAt())
                .resolvedAt(r.getResolvedAt())
                .resolutionNotes(r.getResolutionNotes())
                .version(r.getVersion())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}

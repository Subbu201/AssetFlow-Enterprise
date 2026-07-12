package com.assetflow.audit;

import com.assetflow.activitylog.ActivityLogService;
import com.assetflow.audit.dto.*;
import com.assetflow.audit.gateway.AuditAssetSnapshot;
import com.assetflow.audit.gateway.AssetAuditGateway;
import com.assetflow.auth.repository.UserRepository;
import com.assetflow.common.AuditVerificationStatus;
import com.assetflow.common.Role;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ForbiddenException;
import com.assetflow.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditCycleRepository cycleRepository;
    private final AuditItemRepository itemRepository;
    private final AuditAssignmentRepository assignmentRepository;
    private final AuditDiscrepancyRepository discrepancyRepository;
    private final AssetAuditGateway assetAuditGateway;
    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    public AuditService(AuditCycleRepository cycleRepository,
                        AuditItemRepository itemRepository,
                        AuditAssignmentRepository assignmentRepository,
                        AuditDiscrepancyRepository discrepancyRepository,
                        AssetAuditGateway assetAuditGateway,
                        ActivityLogService activityLogService,
                        UserRepository userRepository) {
        this.cycleRepository = cycleRepository;
        this.itemRepository = itemRepository;
        this.assignmentRepository = assignmentRepository;
        this.discrepancyRepository = discrepancyRepository;
        this.assetAuditGateway = assetAuditGateway;
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
    }

    @Transactional
    public AuditCycleResponse createAuditCycle(CreateAuditCycleRequest request) {
        Authentication auth = requireAuth();
        requireRole(auth, Role.ADMIN);
        validateScope(request);
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("startDate must not be after endDate");
        }
        AuditCycle cycle = new AuditCycle();
        cycle.setAuditCode(request.getAuditCode());
        cycle.setName(request.getName());
        cycle.setScopeType(request.getScopeType());
        cycle.setDepartmentId(request.getDepartmentId());
        cycle.setLocation(request.getLocation());
        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());
        cycle.setStatus("DRAFT");
        cycle.setCreatedByUserId(Long.parseLong(auth.getName()));
        AuditCycle saved = cycleRepository.save(cycle);
        activityLogService.log(getUserId(auth), "AUDIT_CREATED", "AuditCycle", saved.getId(), "Audit cycle created");
        return toResponse(saved);
    }

    public List<AuditCycleResponse> getAuditCycles() {
        return cycleRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AuditCycleResponse getAuditCycle(Long id) {
        return cycleRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Audit cycle not found"));
    }

    @Transactional
    public void assignAuditors(Long auditId, AssignAuditorsRequest request) {
        Authentication auth = requireAuth();
        requireRole(auth, Role.ADMIN);
        AuditCycle cycle = findCycle(auditId);
        ensureNotClosed(cycle);
        for (Long auditorId : request.getAuditorUserIds()) {
            if (!userRepository.existsById(auditorId)) {
                throw new BadRequestException("Auditor user does not exist: " + auditorId);
            }
            if (!assignmentRepository.existsByAuditCycleIdAndAuditorUserId(auditId, auditorId)) {
                AuditAssignment assignment = new AuditAssignment();
                assignment.setAuditCycleId(auditId);
                assignment.setAuditorUserId(auditorId);
                assignment.setAssignedAt(LocalDateTime.now());
                assignmentRepository.save(assignment);
                activityLogService.log(getUserId(auth), "AUDITOR_ASSIGNED", "AuditAssignment", assignment.getId(), "Assigned auditor " + auditorId);
            }
        }
    }

    @Transactional
    public void startAudit(Long auditId) {
        Authentication auth = requireAuth();
        requireRole(auth, Role.ADMIN);
        AuditCycle cycle = findCycle(auditId);
        ensureNotClosed(cycle);
        if (!cycle.getStatus().equals("DRAFT") && !cycle.getStatus().equals("SCHEDULED")) {
            throw new ConflictException("Audit cannot be started from status " + cycle.getStatus());
        }
        List<AuditAssetSnapshot> assets = switch (cycle.getScopeType()) {
            case "DEPARTMENT" -> assetAuditGateway.findAssetsByDepartment(cycle.getDepartmentId());
            case "LOCATION" -> assetAuditGateway.findAssetsByLocation(cycle.getLocation());
            default -> throw new BadRequestException("Unsupported scope type");
        };
        List<AuditItem> items = assets.stream().map(snapshot -> {
            AuditItem item = new AuditItem();
            item.setAuditCycleId(auditId);
            item.setAssetId(snapshot.getAssetId());
            item.setExpectedLocation(snapshot.getLocation());
            item.setExpectedDepartmentId(snapshot.getDepartmentId());
            item.setExpectedAssetStatus(snapshot.getStatus());
            return item;
        }).collect(Collectors.toList());
        itemRepository.saveAll(items);
        cycle.setStatus("IN_PROGRESS");
        cycleRepository.save(cycle);
        activityLogService.log(getUserId(auth), "AUDIT_STARTED", "AuditCycle", auditId, "Audit started with " + items.size() + " items");
    }

    public List<AuditItemResponse> getAuditItems(Long auditId) {
        findCycle(auditId);
        return itemRepository.findByAuditCycleId(auditId).stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    @Transactional
    public AuditItemResponse verifyAuditItem(Long auditId, Long itemId, VerifyAuditItemRequest request) {
        Authentication auth = requireAuth();
        Long userId = getUserId(auth);
        AuditCycle cycle = findCycle(auditId);
        ensureNotClosed(cycle);
        if (!assignmentRepository.existsByAuditCycleIdAndAuditorUserId(auditId, userId)) {
            throw new ForbiddenException("User is not assigned to this audit");
        }
        AuditItem item = itemRepository.findByIdAndAuditCycleId(itemId, auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit item not found"));
        if (request.getVerificationStatus() == null) {
            throw new BadRequestException("Verification status is required");
        }
        item.setVerificationStatus(request.getVerificationStatus());
        item.setActualLocation(request.getActualLocation());
        item.setActualCondition(request.getActualCondition());
        item.setRemarks(request.getRemarks());
        item.setVerifiedByUserId(userId);
        item.setVerifiedAt(LocalDateTime.now());
        itemRepository.save(item);
        if (request.getVerificationStatus() == AuditVerificationStatus.MISSING) {
            createDiscrepancy(cycle, item, "MISSING", "Asset missing during audit");
        } else if (request.getVerificationStatus() == AuditVerificationStatus.DAMAGED) {
            createDiscrepancy(cycle, item, "DAMAGED", "Asset damaged during audit");
        } else if (request.getVerificationStatus() == AuditVerificationStatus.VERIFIED && request.getActualLocation() != null && !request.getActualLocation().equals(item.getExpectedLocation())) {
            createDiscrepancy(cycle, item, "LOCATION_MISMATCH", "Asset location mismatch");
        }
        activityLogService.log(userId, "ITEM_VERIFIED", "AuditItem", item.getId(), "Audit item verified with status " + request.getVerificationStatus());
        return toItemResponse(item);
    }

    public List<AuditDiscrepancyResponse> getDiscrepancies(Long auditId) {
        findCycle(auditId);
        return discrepancyRepository.findByAuditCycleId(auditId).stream().map(this::toDiscrepancyResponse).collect(Collectors.toList());
    }

    @Transactional
    public AuditDiscrepancyResponse resolveDiscrepancy(Long id, ResolveDiscrepancyRequest request) {
        Authentication auth = requireAuth();
        requireRole(auth, Role.ADMIN);
        AuditDiscrepancy discrepancy = discrepancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discrepancy not found"));
        if (discrepancy.getStatus().equals("RESOLVED")) {
            throw new ConflictException("Discrepancy already resolved");
        }
        discrepancy.setResolvedByUserId(request.getResolvedByUserId());
        discrepancy.setResolutionNotes(request.getResolutionNotes());
        discrepancy.setResolvedAt(LocalDateTime.now());
        discrepancy.setStatus("RESOLVED");
        discrepancyRepository.save(discrepancy);
        activityLogService.log(getUserId(auth), "DISCREPANCY_RESOLVED", "AuditDiscrepancy", discrepancy.getId(), "Resolved discrepancy");
        return toDiscrepancyResponse(discrepancy);
    }

    @Transactional
    public void closeAudit(Long auditId) {
        Authentication auth = requireAuth();
        requireRole(auth, Role.ADMIN);
        AuditCycle cycle = findCycle(auditId);
        if (cycle.getStatus().equals("CLOSED") || cycle.getStatus().equals("CANCELLED")) {
            throw new ConflictException("Audit already closed or cancelled");
        }
        long pending = itemRepository.countByAuditCycleIdAndVerificationStatusIsNull(auditId);
        if (pending > 0) {
            throw new ConflictException("Audit cannot close while items are unverified");
        }
        itemRepository.findByAuditCycleId(auditId).forEach(item -> {
            if (item.getVerificationStatus() == AuditVerificationStatus.MISSING) {
                assetAuditGateway.markAssetLost(item.getAssetId(), getUserId(auth), "Confirmed missing asset during audit");
            } else if (item.getVerificationStatus() == AuditVerificationStatus.DAMAGED) {
                assetAuditGateway.markAssetDamaged(item.getAssetId(), getUserId(auth), "Confirmed damaged asset during audit");
            }
        });
        cycle.setStatus("CLOSED");
        cycle.setClosedAt(LocalDateTime.now());
        cycle.setClosedByUserId(getUserId(auth));
        cycleRepository.save(cycle);
        activityLogService.log(getUserId(auth), "AUDIT_CLOSED", "AuditCycle", auditId, "Audit closed");
    }

    public AuditSummaryResponse getAuditSummary(Long auditId) {
        findCycle(auditId);
        long total = itemRepository.countByAuditCycleId(auditId);
        long verified = itemRepository.countByAuditCycleIdAndVerificationStatus(auditId, AuditVerificationStatus.VERIFIED);
        long missing = itemRepository.countByAuditCycleIdAndVerificationStatus(auditId, AuditVerificationStatus.MISSING);
        long damaged = itemRepository.countByAuditCycleIdAndVerificationStatus(auditId, AuditVerificationStatus.DAMAGED);
        long pending = itemRepository.countByAuditCycleIdAndVerificationStatusIsNull(auditId);
        long discrepancies = discrepancyRepository.countByAuditCycleIdAndStatus("OPEN");
        return AuditSummaryResponse.builder()
                .auditCycleId(auditId)
                .totalItems(total)
                .verifiedItems(verified)
                .missingItems(missing)
                .damagedItems(damaged)
                .pendingItems(pending)
                .discrepancyCount(discrepancies)
                .build();
    }

    public AuditProgressResponse getAuditProgress(Long auditId) {
        findCycle(auditId);
        long total = itemRepository.countByAuditCycleId(auditId);
        long verified = itemRepository.countByAuditCycleIdAndVerificationStatus(auditId, AuditVerificationStatus.VERIFIED);
        long missing = itemRepository.countByAuditCycleIdAndVerificationStatus(auditId, AuditVerificationStatus.MISSING);
        long damaged = itemRepository.countByAuditCycleIdAndVerificationStatus(auditId, AuditVerificationStatus.DAMAGED);
        long pending = itemRepository.countByAuditCycleIdAndVerificationStatusIsNull(auditId);
        long discrepancies = discrepancyRepository.countByAuditCycleIdAndStatus("OPEN");
        int completion = total == 0 ? 0 : (int) Math.round((verified + missing + damaged) * 100.0 / total);
        if (completion > 100) {
            completion = 100;
        }
        boolean readyToClose = pending == 0;
        if (cycleRepository.findById(auditId).map(AuditCycle::getStatus).orElse("").equals("CLOSED")) {
            completion = 100;
            readyToClose = true;
        }
        return AuditProgressResponse.builder()
                .totalItems(total)
                .verifiedItems(verified)
                .missingItems(missing)
                .damagedItems(damaged)
                .pendingItems(pending)
                .completionPercentage(completion)
                .discrepancyCount(discrepancies)
                .readyToClose(readyToClose)
                .build();
    }

    private void validateScope(CreateAuditCycleRequest request) {
        if (request.getScopeType().equals("DEPARTMENT")) {
            if (request.getDepartmentId() == null) {
                throw new BadRequestException("departmentId is required for DEPARTMENT scope");
            }
        } else if (request.getScopeType().equals("LOCATION")) {
            if (request.getLocation() == null || request.getLocation().isBlank()) {
                throw new BadRequestException("location is required for LOCATION scope");
            }
        } else {
            throw new BadRequestException("Unsupported scope type");
        }
    }

    private void ensureNotClosed(AuditCycle cycle) {
        if (cycle.getStatus().equals("CLOSED") || cycle.getStatus().equals("CANCELLED")) {
            throw new ConflictException("Closed audits cannot be modified");
        }
    }

    private AuditCycle findCycle(Long auditId) {
        return cycleRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit cycle not found"));
    }

    private AuditCycleResponse toResponse(AuditCycle cycle) {
        return AuditCycleResponse.builder()
                .id(cycle.getId())
                .auditCode(cycle.getAuditCode())
                .name(cycle.getName())
                .scopeType(cycle.getScopeType())
                .departmentId(cycle.getDepartmentId())
                .location(cycle.getLocation())
                .startDate(cycle.getStartDate())
                .endDate(cycle.getEndDate())
                .status(cycle.getStatus())
                .createdByUserId(cycle.getCreatedByUserId())
                .closedByUserId(cycle.getClosedByUserId())
                .closedAt(cycle.getClosedAt())
                .build();
    }

    private AuditItemResponse toItemResponse(AuditItem item) {
        return AuditItemResponse.builder()
                .id(item.getId())
                .auditCycleId(item.getAuditCycleId())
                .assetId(item.getAssetId())
                .expectedLocation(item.getExpectedLocation())
                .expectedDepartmentId(item.getExpectedDepartmentId())
                .expectedAssetStatus(item.getExpectedAssetStatus())
                .verificationStatus(item.getVerificationStatus())
                .actualLocation(item.getActualLocation())
                .actualCondition(item.getActualCondition())
                .verifiedByUserId(item.getVerifiedByUserId())
                .remarks(item.getRemarks())
                .verifiedAt(item.getVerifiedAt())
                .build();
    }

    private AuditDiscrepancyResponse toDiscrepancyResponse(AuditDiscrepancy discrepancy) {
        return AuditDiscrepancyResponse.builder()
                .id(discrepancy.getId())
                .auditCycleId(discrepancy.getAuditCycleId())
                .auditItemId(discrepancy.getAuditItemId())
                .assetId(discrepancy.getAssetId())
                .discrepancyType(discrepancy.getDiscrepancyType())
                .description(discrepancy.getDescription())
                .status(discrepancy.getStatus())
                .resolvedByUserId(discrepancy.getResolvedByUserId())
                .resolutionNotes(discrepancy.getResolutionNotes())
                .resolvedAt(discrepancy.getResolvedAt())
                .build();
    }

    private Authentication requireAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }
        return authentication;
    }

    private void requireRole(Authentication auth, Role role) {
        boolean allowed = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
        if (!allowed) {
            throw new ForbiddenException("Access denied");
        }
    }

    private Long getUserId(Authentication auth) {
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new ForbiddenException("Invalid user id in authentication principal");
        }
    }

    private void createDiscrepancy(AuditCycle auditCycle, AuditItem item, String type, String description) {
        AuditDiscrepancy discrepancy = new AuditDiscrepancy();
        discrepancy.setAuditCycleId(auditCycle.getId());
        discrepancy.setAuditItemId(item.getId());
        discrepancy.setAssetId(item.getAssetId());
        discrepancy.setDiscrepancyType(type);
        discrepancy.setDescription(description);
        discrepancy.setStatus("OPEN");
        discrepancyRepository.save(discrepancy);
        activityLogService.log(getUserId(SecurityContextHolder.getContext().getAuthentication()), "DISCREPANCY_CREATED", "AuditDiscrepancy", discrepancy.getId(), description);
    }
}

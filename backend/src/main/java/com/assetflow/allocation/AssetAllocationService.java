package com.assetflow.allocation;

import com.assetflow.allocation.dto.AllocationConflictResponse;
import com.assetflow.allocation.dto.AllocationResponse;
import com.assetflow.allocation.dto.CreateAllocationRequest;
import com.assetflow.asset.Asset;
import com.assetflow.asset.AssetRepository;
import com.assetflow.assethistory.AssetHistoryService;
import com.assetflow.common.AssetStatus;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assetflow.organization.employee.repository.EmployeeProfileRepository;
import com.assetflow.notification.NotificationService;
import com.assetflow.notification.dto.CreateNotificationRequest;
import com.assetflow.notification.NotificationType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetAllocationService {

    private final AssetAllocationRepository allocationRepository;
    private final AssetRepository assetRepository;
    private final AssetHistoryService assetHistoryService;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final NotificationService notificationService;

    @Transactional
    public AllocationResponse createAllocation(CreateAllocationRequest request) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        
        int qty = asset.getQuantity() != null ? asset.getQuantity() : 1;
        if (qty <= 0 || asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new ConflictException("Asset is already allocated or not available");
        }
        if ((request.getEmployeeId() == null && request.getDepartmentId() == null)
                || (request.getEmployeeId() != null && request.getDepartmentId() != null)) {
            throw new BadRequestException("Exactly one allocation target is required");
        }
        if (request.getExpectedReturnDate() != null && request.getExpectedReturnDate()
                .isBefore(request.getAllocationDate() != null ? request.getAllocationDate() : LocalDate.now())) {
            throw new BadRequestException("Expected return date cannot be before allocation date");
        }

        AssetAllocation allocation = new AssetAllocation();
        allocation.setAssetId(asset.getId());
        allocation.setEmployeeId(request.getEmployeeId());
        allocation.setDepartmentId(request.getDepartmentId());
        allocation.setAllocatedByUserId(request.getAllocatedByUserId());
        allocation
                .setAllocationDate(request.getAllocationDate() != null ? request.getAllocationDate() : LocalDate.now());
        allocation.setExpectedReturnDate(request.getExpectedReturnDate());
        allocation.setStatus("ACTIVE");
        allocation.setNotes(request.getNotes());
        AssetAllocation saved = allocationRepository.save(allocation);

        int newQty = qty - 1;
        asset.setQuantity(newQty);
        if (newQty == 0) {
            asset.setStatus(AssetStatus.ALLOCATED);
        }
        assetRepository.save(asset);
        assetHistoryService.recordAllocation(asset.getId(), request.getAllocatedByUserId(), "Allocation created",
                "Asset allocated");

        if (request.getEmployeeId() != null) {
            employeeProfileRepository.findById(request.getEmployeeId()).ifPresent(profile -> {
                CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
                notificationRequest.setRecipientUserId(profile.getUserAccountId());
                notificationRequest.setType(NotificationType.ASSET_ASSIGNED);
                notificationRequest.setTitle("Asset Allocated");
                notificationRequest.setMessage("Asset " + asset.getName() + " (" + asset.getAssetTag() + ") has been allocated to you.");
                notificationRequest.setReferenceType("ALLOCATION");
                notificationRequest.setReferenceId(saved.getId());
                try {
                    notificationService.createNotification(notificationRequest);
                } catch (Exception ex) {
                    // Log exception, do not fail allocation transaction
                }
            });
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AllocationResponse getAllocation(Long id) {
        return toResponse(allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found")));
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsForAsset(Long assetId) {
        return allocationRepository.findByAssetId(assetId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsForEmployee(Long employeeId) {
        return allocationRepository.findByEmployeeId(employeeId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsForDepartment(Long departmentId) {
        return allocationRepository.findByDepartmentId(departmentId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> getOverdueAllocations() {
        return allocationRepository.findByStatusAndExpectedReturnDateBefore("ACTIVE", LocalDate.now()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public int markOverdueAllocations() {
        List<AssetAllocation> activeAllocations = allocationRepository.findByStatus("ACTIVE");
        int updated = 0;
        for (AssetAllocation allocation : activeAllocations) {
            if (allocation.getExpectedReturnDate() != null
                    && allocation.getExpectedReturnDate().isBefore(LocalDate.now())) {
                allocation.setStatus("OVERDUE");
                allocationRepository.save(allocation);
                updated++;
            }
        }
        return updated;
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllAllocations() {
        return allocationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AllocationConflictResponse buildConflictResponse(Long assetId) {
        Optional<AssetAllocation> current = allocationRepository.findFirstByAssetIdAndStatusIn(assetId,
                List.of("ACTIVE", "RETURN_REQUESTED", "TRANSFER_PENDING"));
        if (current.isEmpty()) {
            return null;
        }
        Asset asset = assetRepository.findById(assetId).orElse(null);
        AllocationConflictResponse response = new AllocationConflictResponse();
        response.setAssetId(assetId);
        response.setAssetTag(asset != null ? asset.getAssetTag() : null);
        response.setCurrentEmployeeId(current.get().getEmployeeId());
        response.setCurrentDepartmentId(current.get().getDepartmentId());
        response.setCurrentAllocationId(current.get().getId());
        response.setMessage("An active allocation exists. A transfer request is required.");
        return response;
    }

    private AllocationResponse toResponse(AssetAllocation allocation) {
        AllocationResponse response = new AllocationResponse();
        response.setId(allocation.getId());
        response.setAssetId(allocation.getAssetId());
        response.setEmployeeId(allocation.getEmployeeId());
        response.setDepartmentId(allocation.getDepartmentId());
        response.setAllocatedByUserId(allocation.getAllocatedByUserId());
        response.setAllocationDate(allocation.getAllocationDate());
        response.setExpectedReturnDate(allocation.getExpectedReturnDate());
        response.setActualReturnDate(allocation.getActualReturnDate());
        response.setStatus(allocation.getStatus());
        response.setCheckoutCondition(allocation.getCheckoutCondition());
        response.setNotes(allocation.getNotes());
        response.setVersion(allocation.getVersion());
        response.setCreatedAt(allocation.getCreatedAt());
        response.setUpdatedAt(allocation.getUpdatedAt());
        return response;
    }
}

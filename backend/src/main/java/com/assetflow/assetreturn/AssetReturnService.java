package com.assetflow.assetreturn;

import com.assetflow.allocation.AssetAllocation;
import com.assetflow.allocation.AssetAllocationRepository;
import com.assetflow.asset.Asset;
import com.assetflow.asset.AssetRepository;
import com.assetflow.assetreturn.dto.ApproveReturnRequest;
import com.assetflow.assetreturn.dto.AssetReturnResponse;
import com.assetflow.assetreturn.dto.CreateReturnRequest;
import com.assetflow.assetreturn.dto.RejectReturnRequest;
import com.assetflow.assethistory.AssetHistoryService;
import com.assetflow.common.AssetStatus;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetReturnService {

    private final AssetReturnRepository returnRepository;
    private final AssetAllocationRepository allocationRepository;
    private final AssetRepository assetRepository;
    private final AssetHistoryService assetHistoryService;

    @Transactional
    public AssetReturnResponse requestReturn(CreateReturnRequest request) {
        AssetAllocation allocation = allocationRepository.findById(request.getAllocationId()).orElseThrow(() -> new ResourceNotFoundException("Allocation not found"));
        if (!"ACTIVE".equals(allocation.getStatus())) {
            throw new BadRequestException("Only an active allocation can be returned");
        }
        Optional<AssetReturn> existing = returnRepository.findFirstByAllocationIdAndStatusIn(request.getAllocationId(), List.of("REQUESTED", "APPROVED"));
        if (existing.isPresent()) {
            throw new ConflictException("Return request already exists for this allocation");
        }
        AssetReturn assetReturn = new AssetReturn();
        assetReturn.setAllocationId(request.getAllocationId());
        assetReturn.setAssetId(request.getAssetId());
        assetReturn.setRequestedByUserId(request.getRequestedByUserId());
        assetReturn.setRequestedAt(request.getRequestedAt() != null ? request.getRequestedAt() : LocalDateTime.now());
        assetReturn.setReturnCondition(request.getReturnCondition());
        assetReturn.setCheckInNotes(request.getCheckInNotes());
        assetReturn.setStatus("REQUESTED");
        AssetReturn saved = returnRepository.save(assetReturn);
        assetHistoryService.recordReturn(request.getAssetId(), request.getRequestedByUserId(), "Return requested", "Return request created");
        return toResponse(saved);
    }

    @Transactional
    public AssetReturnResponse approveReturn(Long id, ApproveReturnRequest request) {
        AssetReturn assetReturn = returnRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
        if (!"REQUESTED".equals(assetReturn.getStatus())) {
            throw new BadRequestException("Only requested returns can be approved");
        }
        AssetAllocation allocation = allocationRepository.findById(assetReturn.getAllocationId()).orElseThrow(() -> new ResourceNotFoundException("Allocation not found"));
        allocation.setStatus("RETURNED");
        allocation.setActualReturnDate(LocalDateTime.now().toLocalDate());
        allocationRepository.save(allocation);

        Asset asset = assetRepository.findById(assetReturn.getAssetId()).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        asset.setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(asset);

        assetReturn.setStatus("APPROVED");
        assetReturn.setApprovedByUserId(request.getApprovedByUserId());
        assetReturn.setApprovedAt(request.getApprovedAt() != null ? request.getApprovedAt() : LocalDateTime.now());
        assetReturn.setCheckInNotes(request.getCheckInNotes());
        AssetReturn saved = returnRepository.save(assetReturn);
        assetHistoryService.recordReturn(asset.getId(), request.getApprovedByUserId(), "Return approved", "Asset returned");
        return toResponse(saved);
    }

    @Transactional
    public AssetReturnResponse rejectReturn(Long id, RejectReturnRequest request) {
        AssetReturn assetReturn = returnRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
        if (!"REQUESTED".equals(assetReturn.getStatus())) {
            throw new BadRequestException("Only requested returns can be rejected");
        }
        assetReturn.setStatus("REJECTED");
        assetReturn.setApprovedByUserId(request.getApprovedByUserId());
        AssetReturn saved = returnRepository.save(assetReturn);
        assetHistoryService.recordReturn(assetReturn.getAssetId(), request.getApprovedByUserId(), "Return rejected", "Return rejected");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AssetReturnResponse getReturn(Long id) {
        return toResponse(returnRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Return request not found")));
    }

    @Transactional(readOnly = true)
    public List<AssetReturnResponse> getPendingReturns() {
        return returnRepository.findByStatus("REQUESTED").stream().map(this::toResponse).toList();
    }

    private AssetReturnResponse toResponse(AssetReturn entity) {
        AssetReturnResponse response = new AssetReturnResponse();
        response.setId(entity.getId());
        response.setAllocationId(entity.getAllocationId());
        response.setAssetId(entity.getAssetId());
        response.setRequestedByUserId(entity.getRequestedByUserId());
        response.setRequestedAt(entity.getRequestedAt());
        response.setReturnCondition(entity.getReturnCondition());
        response.setCheckInNotes(entity.getCheckInNotes());
        response.setStatus(entity.getStatus());
        response.setApprovedByUserId(entity.getApprovedByUserId());
        response.setApprovedAt(entity.getApprovedAt());
        return response;
    }
}

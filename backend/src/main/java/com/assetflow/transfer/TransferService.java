package com.assetflow.transfer;

import com.assetflow.allocation.AssetAllocation;
import com.assetflow.allocation.AssetAllocationRepository;
import com.assetflow.asset.Asset;
import com.assetflow.asset.AssetRepository;
import com.assetflow.assethistory.AssetHistoryService;
import com.assetflow.common.AssetStatus;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.transfer.dto.CreateTransferRequest;
import com.assetflow.transfer.dto.ReviewTransferRequest;
import com.assetflow.transfer.dto.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRequestRepository transferRepository;
    private final AssetAllocationRepository allocationRepository;
    private final AssetRepository assetRepository;
    private final AssetHistoryService assetHistoryService;

    @Transactional
    public TransferResponse requestTransfer(CreateTransferRequest request) {
        AssetAllocation allocation = allocationRepository.findById(request.getAllocationId()).orElseThrow(() -> new ResourceNotFoundException("Allocation not found"));
        if (!"ACTIVE".equals(allocation.getStatus())) {
            throw new BadRequestException("Transfer requires an active allocation");
        }
        if ((request.getTargetEmployeeId() == null && request.getTargetDepartmentId() == null) || (request.getTargetEmployeeId() != null && request.getTargetDepartmentId() != null)) {
            throw new BadRequestException("Exactly one target is required");
        }
        if (request.getTargetEmployeeId() != null && request.getTargetEmployeeId().equals(allocation.getEmployeeId())) {
            throw new BadRequestException("Target cannot be the current holder");
        }
        if (request.getTargetDepartmentId() != null && request.getTargetDepartmentId().equals(allocation.getDepartmentId())) {
            throw new BadRequestException("Target cannot be the current holder");
        }

        allocation.setStatus("TRANSFER_PENDING");
        allocationRepository.save(allocation);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setAllocationId(request.getAllocationId());
        transferRequest.setAssetId(request.getAssetId());
        transferRequest.setRequestedByUserId(request.getRequestedByUserId());
        transferRequest.setTargetEmployeeId(request.getTargetEmployeeId());
        transferRequest.setTargetDepartmentId(request.getTargetDepartmentId());
        transferRequest.setReason(request.getReason());
        transferRequest.setStatus("REQUESTED");
        TransferRequest saved = transferRepository.save(transferRequest);
        assetHistoryService.recordTransfer(request.getAssetId(), request.getRequestedByUserId(), "Transfer requested", "Transfer request created");
        return toResponse(saved);
    }

    @Transactional
    public TransferResponse reviewTransfer(Long id, ReviewTransferRequest request) {
        TransferRequest transferRequest = transferRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transfer request not found"));
        if (!"REQUESTED".equals(transferRequest.getStatus())) {
            throw new ConflictException("Transfer is already reviewed");
        }
        AssetAllocation allocation = allocationRepository.findById(transferRequest.getAllocationId()).orElseThrow(() -> new ResourceNotFoundException("Allocation not found"));

        if (request.isApproved()) {
            allocation.setStatus("TRANSFERRED");
            allocationRepository.save(allocation);
            TransferRequest newTransfer = new TransferRequest();
            newTransfer.setAllocationId(transferRequest.getAllocationId());
            newTransfer.setAssetId(transferRequest.getAssetId());
            newTransfer.setRequestedByUserId(transferRequest.getRequestedByUserId());
            newTransfer.setTargetEmployeeId(transferRequest.getTargetEmployeeId());
            newTransfer.setTargetDepartmentId(transferRequest.getTargetDepartmentId());
            newTransfer.setReason(transferRequest.getReason());
            newTransfer.setStatus("APPROVED");
            transferRepository.save(newTransfer);

            AssetAllocation newAllocation = new AssetAllocation();
            newAllocation.setAssetId(transferRequest.getAssetId());
            newAllocation.setEmployeeId(transferRequest.getTargetEmployeeId());
            newAllocation.setDepartmentId(transferRequest.getTargetDepartmentId());
            newAllocation.setAllocatedByUserId(transferRequest.getRequestedByUserId());
            newAllocation.setAllocationDate(java.time.LocalDate.now());
            newAllocation.setExpectedReturnDate(allocation.getExpectedReturnDate());
            newAllocation.setStatus("ACTIVE");
            newAllocation.setNotes("Transferred from allocation " + allocation.getId());
            allocationRepository.save(newAllocation);

            Asset asset = assetRepository.findById(transferRequest.getAssetId()).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
            asset.setStatus(AssetStatus.ALLOCATED);
            assetRepository.save(asset);
            assetHistoryService.recordTransfer(asset.getId(), transferRequest.getRequestedByUserId(), "Transfer approved", "Transfer approved");
            transferRequest.setStatus("APPROVED");
            transferRequest.setReviewedByUserId(request.getReviewedByUserId());
            transferRequest.setReviewedAt(request.getReviewedAt());
            transferRequest.setReviewComments(request.getReviewComments());
            return toResponse(transferRepository.save(transferRequest));
        }

        transferRequest.setStatus("REJECTED");
        transferRequest.setReviewedByUserId(request.getReviewedByUserId());
        transferRequest.setReviewedAt(request.getReviewedAt());
        transferRequest.setReviewComments(request.getReviewComments());
        allocation.setStatus("ACTIVE");
        allocationRepository.save(allocation);
        assetHistoryService.recordTransfer(transferRequest.getAssetId(), request.getReviewedByUserId(), "Transfer rejected", "Transfer rejected");
        return toResponse(transferRepository.save(transferRequest));
    }

    @Transactional(readOnly = true)
    public TransferResponse getTransfer(Long id) {
        return toResponse(transferRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transfer request not found")));
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> getPendingTransfers() {
        return transferRepository.findByStatus("REQUESTED").stream().map(this::toResponse).toList();
    }

    private TransferResponse toResponse(TransferRequest entity) {
        TransferResponse response = new TransferResponse();
        response.setId(entity.getId());
        response.setAllocationId(entity.getAllocationId());
        response.setAssetId(entity.getAssetId());
        response.setRequestedByUserId(entity.getRequestedByUserId());
        response.setTargetEmployeeId(entity.getTargetEmployeeId());
        response.setTargetDepartmentId(entity.getTargetDepartmentId());
        response.setReason(entity.getReason());
        response.setStatus(entity.getStatus());
        response.setReviewedByUserId(entity.getReviewedByUserId());
        response.setReviewedAt(entity.getReviewedAt());
        response.setReviewComments(entity.getReviewComments());
        return response;
    }
}

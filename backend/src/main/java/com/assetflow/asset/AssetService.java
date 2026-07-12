package com.assetflow.asset;

import com.assetflow.asset.dto.*;
import com.assetflow.assethistory.AssetHistoryService;
import com.assetflow.assethistory.dto.AssetHistoryResponse;
import com.assetflow.common.AssetStatus;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetHistoryService assetHistoryService;
    private final com.assetflow.auth.repository.UserRepository userRepository;

    @Transactional
    public Asset createAsset(CreateAssetRequest request) {
        if (request.getAcquisitionCost() != null && request.getAcquisitionCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Acquisition cost cannot be negative");
        }
        
        String serialNumber = request.getSerialNumber();
        if (serialNumber != null && serialNumber.trim().isEmpty()) {
            serialNumber = null;
        }
        
        if (serialNumber != null && assetRepository.existsBySerialNumber(serialNumber)) {
            throw new ConflictException("Asset with this serial number already exists");
        }

        Asset asset = new Asset();
        asset.setName(request.getName());
        asset.setCategoryId(request.getCategoryId());
        asset.setSerialNumber(serialNumber);
        asset.setAcquisitionDate(request.getAcquisitionDate());
        asset.setAcquisitionCost(request.getAcquisitionCost());
        asset.setCondition(request.getCondition());
        asset.setLocation(request.getLocation());
        asset.setDepartmentId(request.getDepartmentId());
        asset.setSharedBookable(request.isSharedBookable());
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setPhotoUrl(request.getPhotoUrl());
        asset.setDocumentUrl(request.getDocumentUrl());
        asset.setManufacturer(request.getManufacturer());
        asset.setModel(request.getModel());
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        asset.setNotes(request.getNotes());
        asset.setRegisteredByUserId(request.getRegisteredByUserId());
        asset.setAssetTag(generateAssetTag());

        Asset saved = assetRepository.save(asset);
        assetHistoryService.recordAssetRegistration(saved.getId(), saved.getRegisteredByUserId(), "Asset registered",
                "Registration");
        return saved;
    }

    @Transactional(readOnly = true)
    public Asset getAsset(Long id) {
        return assetRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
    }

    @Transactional(readOnly = true)
    public Page<AssetResponse> searchAssets(String keyword, String assetTag, String serialNumber, Long categoryId,
            String status, Long departmentId, String location, Boolean sharedBookable,
            int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size,
                sort == null ? Sort.by(Sort.Direction.DESC, "createdAt") : Sort.by(Sort.Direction.DESC, sort));
        AssetStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            parsedStatus = AssetStatus.valueOf(status.toUpperCase());
        }
        return assetRepository
                .search(keyword, assetTag, serialNumber, categoryId, parsedStatus, departmentId, location,
                        sharedBookable, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public Asset updateAsset(Long id, UpdateAssetRequest request) {
        Asset asset = getAsset(id);
        if (request.getName() != null)
            asset.setName(request.getName());
        if (request.getCategoryId() != null)
            asset.setCategoryId(request.getCategoryId());
        if (request.getSerialNumber() != null) {
            String reqSerial = request.getSerialNumber();
            if (reqSerial.trim().isEmpty()) {
                reqSerial = null;
            }
            if (reqSerial != null) {
                if (assetRepository.existsBySerialNumber(reqSerial)
                        && !reqSerial.equals(asset.getSerialNumber())) {
                    throw new ConflictException("Asset with this serial number already exists");
                }
                asset.setSerialNumber(reqSerial);
            } else {
                asset.setSerialNumber(null);
            }
        }
        if (request.getAcquisitionDate() != null)
            asset.setAcquisitionDate(request.getAcquisitionDate());
        if (request.getAcquisitionCost() != null) {
            if (request.getAcquisitionCost().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Acquisition cost cannot be negative");
            }
            asset.setAcquisitionCost(request.getAcquisitionCost());
        }
        if (request.getCondition() != null)
            asset.setCondition(request.getCondition());
        if (request.getLocation() != null)
            asset.setLocation(request.getLocation());
        if (request.getDepartmentId() != null)
            asset.setDepartmentId(request.getDepartmentId());
        if (request.getSharedBookable() != null)
            asset.setSharedBookable(request.getSharedBookable());
        if (request.getPhotoUrl() != null)
            asset.setPhotoUrl(request.getPhotoUrl());
        if (request.getDocumentUrl() != null)
            asset.setDocumentUrl(request.getDocumentUrl());
        if (request.getManufacturer() != null)
            asset.setManufacturer(request.getManufacturer());
        if (request.getModel() != null)
            asset.setModel(request.getModel());
        if (request.getWarrantyExpiryDate() != null)
            asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        if (request.getNotes() != null)
            asset.setNotes(request.getNotes());
        return assetRepository.save(asset);
    }

    @Transactional
    public Asset updateAssetStatus(Long id, UpdateAssetStatusRequest request) {
        Asset asset = getAsset(id);
        AssetStatus oldStatus = asset.getStatus();
        asset.setStatus(request.getStatus());
        Asset saved = assetRepository.save(asset);
        assetHistoryService.recordStatusChange(saved.getId(), asset.getRegisteredByUserId(), oldStatus,
                saved.getStatus(), "status changed", "Status updated");
        return saved;
    }

    @Transactional
    public Asset updateAssetLocation(Long id, UpdateAssetLocationRequest request) {
        Asset asset = getAsset(id);
        String oldValue = asset.getLocation();
        asset.setLocation(request.getLocation());
        Asset saved = assetRepository.save(asset);
        assetHistoryService.recordLocationChange(saved.getId(), asset.getRegisteredByUserId(), oldValue,
                saved.getLocation(), "location changed", "Location updated");
        return saved;
    }

    @Transactional
    public Asset updateAssetCondition(Long id, UpdateAssetConditionRequest request) {
        Asset asset = getAsset(id);
        String oldCondition = asset.getCondition();
        asset.setCondition(request.getCondition());
        Asset saved = assetRepository.save(asset);
        assetHistoryService.recordConditionChange(saved.getId(), asset.getRegisteredByUserId(), oldCondition,
                saved.getCondition(), "condition changed", "Condition updated");
        return saved;
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetResponse(Long id) {
        return toResponse(getAsset(id));
    }

    @Transactional(readOnly = true)
    public Asset findByAssetTag(String assetTag) {
        return assetRepository.findByAssetTag(assetTag)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
    }

    @Transactional(readOnly = true)
    public List<AssetHistoryResponse> getAssetTimeline(Long assetId) {
        return assetHistoryService.getAssetTimeline(assetId);
    }

    private String generateAssetTag() {
        return "AF-" + String.format("%04d", System.currentTimeMillis() % 10000);
    }

    private AssetResponse toResponse(Asset asset) {
        AssetResponse response = new AssetResponse();
        response.setId(asset.getId());
        response.setAssetTag(asset.getAssetTag());
        response.setName(asset.getName());
        response.setCategoryId(asset.getCategoryId());
        response.setSerialNumber(asset.getSerialNumber());
        response.setAcquisitionDate(asset.getAcquisitionDate());
        response.setAcquisitionCost(asset.getAcquisitionCost());
        response.setCondition(asset.getCondition());
        response.setLocation(asset.getLocation());
        response.setDepartmentId(asset.getDepartmentId());
        response.setSharedBookable(asset.isSharedBookable());
        response.setStatus(asset.getStatus());
        response.setPhotoUrl(asset.getPhotoUrl());
        response.setDocumentUrl(asset.getDocumentUrl());
        response.setManufacturer(asset.getManufacturer());
        response.setModel(asset.getModel());
        response.setWarrantyExpiryDate(asset.getWarrantyExpiryDate());
        response.setNotes(asset.getNotes());
        response.setRegisteredByUserId(asset.getRegisteredByUserId());
        if (asset.getRegisteredByUserId() != null) {
            userRepository.findById(asset.getRegisteredByUserId()).ifPresent(user -> {
                response.setRegisteredByUserEmail(user.getEmail());
                response.setRegisteredByUserName(user.getFullName());
            });
        }
        response.setVersion(asset.getVersion());
        response.setCreatedAt(asset.getCreatedAt());
        response.setUpdatedAt(asset.getUpdatedAt());
        return response;
    }
}

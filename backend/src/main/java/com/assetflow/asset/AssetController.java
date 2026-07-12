package com.assetflow.asset;

import com.assetflow.asset.dto.*;
import com.assetflow.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AssetResponse> createAsset(@Valid @RequestBody CreateAssetRequest request) {
        return ApiResponse.success("Asset created", toResponse(assetService.createAsset(request)));
    }

    @GetMapping
    public ApiResponse<Page<AssetResponse>> getAssets(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String assetTag,
                                                      @RequestParam(required = false) String serialNumber,
                                                      @RequestParam(required = false) Long categoryId,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) Long departmentId,
                                                      @RequestParam(required = false) String location,
                                                      @RequestParam(required = false) Boolean sharedBookable,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      @RequestParam(defaultValue = "id") String sort) {
        return ApiResponse.success("Assets retrieved", assetService.searchAssets(keyword, assetTag, serialNumber, categoryId, status, departmentId, location, sharedBookable, page, size, sort));
    }

    @GetMapping("/{id}")
    public ApiResponse<AssetResponse> getAsset(@PathVariable Long id) {
        return ApiResponse.success("Asset retrieved", assetService.getAssetResponse(id));
    }

    @GetMapping("/tag/{assetTag}")
    public ApiResponse<AssetResponse> getAssetByTag(@PathVariable String assetTag) {
        return ApiResponse.success("Asset retrieved", toResponse(assetService.findByAssetTag(assetTag)));
    }

    @PutMapping("/{id}")
    public ApiResponse<AssetResponse> updateAsset(@PathVariable Long id, @Valid @RequestBody UpdateAssetRequest request) {
        return ApiResponse.success("Asset updated", toResponse(assetService.updateAsset(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<AssetResponse> updateAssetStatus(@PathVariable Long id, @Valid @RequestBody UpdateAssetStatusRequest request) {
        return ApiResponse.success("Asset status updated", toResponse(assetService.updateAssetStatus(id, request)));
    }

    @PatchMapping("/{id}/location")
    public ApiResponse<AssetResponse> updateAssetLocation(@PathVariable Long id, @Valid @RequestBody UpdateAssetLocationRequest request) {
        return ApiResponse.success("Asset location updated", toResponse(assetService.updateAssetLocation(id, request)));
    }

    @PatchMapping("/{id}/condition")
    public ApiResponse<AssetResponse> updateAssetCondition(@PathVariable Long id, @Valid @RequestBody UpdateAssetConditionRequest request) {
        return ApiResponse.success("Asset condition updated", toResponse(assetService.updateAssetCondition(id, request)));
    }

    @GetMapping("/{assetId}/timeline")
    public ApiResponse<List<?>> getTimeline(@PathVariable Long assetId) {
        return ApiResponse.success("Timeline retrieved", assetService.getAssetTimeline(assetId));
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
        response.setVersion(asset.getVersion());
        response.setCreatedAt(asset.getCreatedAt());
        response.setUpdatedAt(asset.getUpdatedAt());
        return response;
    }
}

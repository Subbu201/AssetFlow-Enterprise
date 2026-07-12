package com.assetflow.allocation;

import com.assetflow.allocation.dto.AllocationConflictResponse;
import com.assetflow.allocation.dto.AllocationResponse;
import com.assetflow.allocation.dto.CreateAllocationRequest;
import com.assetflow.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class AssetAllocationController {

    private final AssetAllocationService allocationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<?> createAllocation(@Valid @RequestBody CreateAllocationRequest request) {
        try {
            return ApiResponse.success("Allocation created", allocationService.createAllocation(request));
        } catch (RuntimeException ex) {
            if (ex instanceof com.assetflow.exception.ConflictException) {
                AllocationConflictResponse conflict = allocationService.buildConflictResponse(request.getAssetId());
                return ApiResponse.error(ex.getMessage());
            }
            throw ex;
        }
    }

    @GetMapping
    public ApiResponse<List<AllocationResponse>> getAllocations() {
        return ApiResponse.success("Allocations retrieved", allocationService.getAllAllocations());
    }

    @GetMapping("/{id}")
    public ApiResponse<AllocationResponse> getAllocation(@PathVariable Long id) {
        return ApiResponse.success("Allocation retrieved", allocationService.getAllocation(id));
    }

    @GetMapping("/asset/{assetId}")
    public ApiResponse<List<AllocationResponse>> getAllocationsForAsset(@PathVariable Long assetId) {
        return ApiResponse.success("Asset allocations retrieved", allocationService.getAllocationsForAsset(assetId));
    }

    @GetMapping("/employee/{employeeId}")
    public ApiResponse<List<AllocationResponse>> getAllocationsForEmployee(@PathVariable Long employeeId) {
        return ApiResponse.success("Employee allocations retrieved", allocationService.getAllocationsForEmployee(employeeId));
    }

    @GetMapping("/department/{departmentId}")
    public ApiResponse<List<AllocationResponse>> getAllocationsForDepartment(@PathVariable Long departmentId) {
        return ApiResponse.success("Department allocations retrieved", allocationService.getAllocationsForDepartment(departmentId));
    }

    @GetMapping("/overdue")
    public ApiResponse<List<AllocationResponse>> getOverdueAllocations() {
        return ApiResponse.success("Overdue allocations retrieved", allocationService.getOverdueAllocations());
    }

    @PostMapping("/overdue/run")
    public ApiResponse<Map<String, Integer>> markOverdue() {
        return ApiResponse.success("Overdue allocations updated", Map.of("updatedCount", allocationService.markOverdueAllocations()));
    }
}

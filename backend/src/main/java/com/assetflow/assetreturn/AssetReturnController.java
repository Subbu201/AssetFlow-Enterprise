package com.assetflow.assetreturn;

import com.assetflow.assetreturn.dto.ApproveReturnRequest;
import com.assetflow.assetreturn.dto.AssetReturnResponse;
import com.assetflow.assetreturn.dto.CreateReturnRequest;
import com.assetflow.assetreturn.dto.RejectReturnRequest;
import com.assetflow.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class AssetReturnController {

    private final AssetReturnService returnService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AssetReturnResponse> requestReturn(@Valid @RequestBody CreateReturnRequest request) {
        return ApiResponse.success("Return request created", returnService.requestReturn(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<AssetReturnResponse> getReturn(@PathVariable Long id) {
        return ApiResponse.success("Return request retrieved", returnService.getReturn(id));
    }

    @GetMapping("/pending")
    public ApiResponse<List<AssetReturnResponse>> getPendingReturns() {
        return ApiResponse.success("Pending returns retrieved", returnService.getPendingReturns());
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<AssetReturnResponse> approveReturn(@PathVariable Long id, @RequestBody ApproveReturnRequest request) {
        return ApiResponse.success("Return approved", returnService.approveReturn(id, request));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<AssetReturnResponse> rejectReturn(@PathVariable Long id, @RequestBody RejectReturnRequest request) {
        return ApiResponse.success("Return rejected", returnService.rejectReturn(id, request));
    }
}

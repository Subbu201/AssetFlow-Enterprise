package com.assetflow.transfer;

import com.assetflow.common.ApiResponse;
import com.assetflow.transfer.dto.CreateTransferRequest;
import com.assetflow.transfer.dto.ReviewTransferRequest;
import com.assetflow.transfer.dto.TransferResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransferResponse> requestTransfer(@Valid @RequestBody CreateTransferRequest request) {
        return ApiResponse.success("Transfer request created", transferService.requestTransfer(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransferResponse> getTransfer(@PathVariable Long id) {
        return ApiResponse.success("Transfer retrieved", transferService.getTransfer(id));
    }

    @GetMapping("/pending")
    public ApiResponse<List<TransferResponse>> getPendingTransfers() {
        return ApiResponse.success("Pending transfers retrieved", transferService.getPendingTransfers());
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<TransferResponse> approveTransfer(@PathVariable Long id, @RequestBody ReviewTransferRequest request) {
        return ApiResponse.success("Transfer approved", transferService.reviewTransfer(id, request));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<TransferResponse> rejectTransfer(@PathVariable Long id, @RequestBody ReviewTransferRequest request) {
        return ApiResponse.success("Transfer rejected", transferService.reviewTransfer(id, request));
    }
}

package com.assetflow.assethistory;

import com.assetflow.assethistory.dto.AssetHistoryResponse;
import com.assetflow.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetHistoryController {

    private final AssetHistoryService assetHistoryService;

    @GetMapping("/{assetId}/history")
    public ApiResponse<List<AssetHistoryResponse>> getHistory(@PathVariable Long assetId) {
        return ApiResponse.success("Asset history retrieved", assetHistoryService.getHistory(assetId));
    }
}

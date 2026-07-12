package com.assetflow.audit;

import com.assetflow.audit.dto.*;
import com.assetflow.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public ApiResponse<AuditCycleResponse> createAuditCycle(@Valid @RequestBody CreateAuditCycleRequest request) {
        return ApiResponse.success("Audit cycle created", auditService.createAuditCycle(request));
    }

    @GetMapping
    public ApiResponse<List<AuditCycleResponse>> getAuditCycles() {
        return ApiResponse.success("Audit cycles returned", auditService.getAuditCycles());
    }

    @GetMapping("/{id}")
    public ApiResponse<AuditCycleResponse> getAuditCycle(@PathVariable Long id) {
        return ApiResponse.success("Audit cycle returned", auditService.getAuditCycle(id));
    }

    @PostMapping("/{id}/assign-auditors")
    public ApiResponse<String> assignAuditors(@PathVariable Long id, @Valid @RequestBody AssignAuditorsRequest request) {
        auditService.assignAuditors(id, request);
        return ApiResponse.success("Auditors assigned", null);
    }

    @PostMapping("/{id}/start")
    public ApiResponse<String> startAudit(@PathVariable Long id) {
        auditService.startAudit(id);
        return ApiResponse.success("Audit started", null);
    }

    @GetMapping("/{id}/items")
    public ApiResponse<List<AuditItemResponse>> getAuditItems(@PathVariable Long id) {
        return ApiResponse.success("Audit items returned", auditService.getAuditItems(id));
    }

    @PatchMapping("/{auditId}/items/{itemId}/verify")
    public ApiResponse<AuditItemResponse> verifyAuditItem(@PathVariable Long auditId,
                                                          @PathVariable Long itemId,
                                                          @Valid @RequestBody VerifyAuditItemRequest request) {
        return ApiResponse.success("Audit item verified", auditService.verifyAuditItem(auditId, itemId, request));
    }

    @GetMapping("/{id}/discrepancies")
    public ApiResponse<List<AuditDiscrepancyResponse>> getDiscrepancies(@PathVariable Long id) {
        return ApiResponse.success("Audit discrepancies returned", auditService.getDiscrepancies(id));
    }

    @PatchMapping("/discrepancies/{id}/resolve")
    public ApiResponse<AuditDiscrepancyResponse> resolveDiscrepancy(@PathVariable Long id,
                                                                   @Valid @RequestBody ResolveDiscrepancyRequest request) {
        return ApiResponse.success("Discrepancy resolved", auditService.resolveDiscrepancy(id, request));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<String> closeAudit(@PathVariable Long id) {
        auditService.closeAudit(id);
        return ApiResponse.success("Audit closed", null);
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<AuditSummaryResponse> getAuditSummary(@PathVariable Long id) {
        return ApiResponse.success("Audit summary returned", auditService.getAuditSummary(id));
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<AuditProgressResponse> getAuditProgress(@PathVariable Long id) {
        return ApiResponse.success("Audit progress returned", auditService.getAuditProgress(id));
    }
}

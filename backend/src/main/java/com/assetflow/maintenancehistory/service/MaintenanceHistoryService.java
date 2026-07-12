package com.assetflow.maintenancehistory.service;

import com.assetflow.common.MaintenanceStatus;
import com.assetflow.maintenancehistory.dto.MaintenanceHistoryResponse;
import com.assetflow.maintenancehistory.entity.MaintenanceHistory;
import com.assetflow.maintenancehistory.repository.MaintenanceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceHistoryService {

    private final MaintenanceHistoryRepository historyRepository;

    /**
     * Records a status transition event for a maintenance request.
     * Called by {@link com.assetflow.maintenance.service.MaintenanceService}
     * on every state change.
     *
     * @param maintenanceRequestId the maintenance request that changed state
     * @param assetId              denormalised asset ID
     * @param previousStatus       state before the transition (null for creation)
     * @param newStatus            state after the transition
     * @param actionByUserId       user who performed the action
     * @param comments             optional human-readable comments
     */
    @Transactional
    public void recordEvent(
            Long maintenanceRequestId,
            Long assetId,
            MaintenanceStatus previousStatus,
            MaintenanceStatus newStatus,
            Long actionByUserId,
            String comments) {

        MaintenanceHistory history = MaintenanceHistory.builder()
                .maintenanceRequestId(maintenanceRequestId)
                .assetId(assetId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .actionByUserId(actionByUserId)
                .comments(comments)
                .eventTime(LocalDateTime.now())
                .build();

        historyRepository.save(history);
        log.debug("History recorded for request {} : {} → {}", maintenanceRequestId, previousStatus, newStatus);
    }

    /**
     * Returns history for a maintenance request, newest first.
     */
    public Page<MaintenanceHistoryResponse> getHistory(Long maintenanceRequestId, Pageable pageable) {
        return historyRepository
                .findByMaintenanceRequestIdOrderByEventTimeDesc(maintenanceRequestId, pageable)
                .map(this::toResponse);
    }

    private MaintenanceHistoryResponse toResponse(MaintenanceHistory h) {
        return MaintenanceHistoryResponse.builder()
                .id(h.getId())
                .maintenanceRequestId(h.getMaintenanceRequestId())
                .assetId(h.getAssetId())
                .previousStatus(h.getPreviousStatus())
                .newStatus(h.getNewStatus())
                .actionByUserId(h.getActionByUserId())
                .comments(h.getComments())
                .eventTime(h.getEventTime())
                .createdAt(h.getCreatedAt())
                .build();
    }
}

package com.assetflow.assethistory;

import com.assetflow.asset.dto.AssetResponse;
import com.assetflow.assethistory.dto.AssetHistoryResponse;
import com.assetflow.common.AssetStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetHistoryService {

    private final AssetHistoryRepository assetHistoryRepository;

    @Transactional
    public void recordAssetRegistration(Long assetId, Long userId, String action, String remarks) {
        saveHistory(assetId, action, null, null, null, null, remarks, userId, LocalDateTime.now());
    }

    @Transactional
    public void recordStatusChange(Long assetId, Long userId, AssetStatus previousStatus, AssetStatus newStatus,
            String action, String remarks) {
        saveHistory(assetId, action, previousStatus, newStatus, previousStatus != null ? previousStatus.name() : null,
                newStatus != null ? newStatus.name() : null, remarks, userId, LocalDateTime.now());
    }

    @Transactional
    public void recordLocationChange(Long assetId, Long userId, String previousValue, String newValue, String action,
            String remarks) {
        saveHistory(assetId, action, null, null, previousValue, newValue, remarks, userId, LocalDateTime.now());
    }

    @Transactional
    public void recordConditionChange(Long assetId, Long userId, String previousValue, String newValue, String action,
            String remarks) {
        saveHistory(assetId, action, null, null, previousValue, newValue, remarks, userId, LocalDateTime.now());
    }

    @Transactional
    public void recordAllocation(Long assetId, Long userId, String action, String remarks) {
        saveHistory(assetId, action, null, null, null, null, remarks, userId, LocalDateTime.now());
    }

    @Transactional
    public void recordReturn(Long assetId, Long userId, String action, String remarks) {
        saveHistory(assetId, action, null, null, null, null, remarks, userId, LocalDateTime.now());
    }

    @Transactional
    public void recordTransfer(Long assetId, Long userId, String action, String remarks) {
        saveHistory(assetId, action, null, null, null, null, remarks, userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<AssetHistoryResponse> getHistory(Long assetId) {
        return assetHistoryRepository.findByAssetIdOrderByEventTimeDescCreatedAtDescIdDesc(assetId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssetHistoryResponse> getAssetTimeline(Long assetId) {
        return getHistory(assetId);
    }

    private void saveHistory(Long assetId, String action, AssetStatus previousStatus, AssetStatus newStatus,
            String previousValue, String newValue, String remarks, Long performedByUserId, LocalDateTime eventTime) {
        AssetHistory history = new AssetHistory();
        history.setAssetId(assetId);
        history.setAction(action);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setPreviousValue(previousValue);
        history.setNewValue(newValue);
        history.setRemarks(remarks);
        history.setPerformedByUserId(performedByUserId);
        history.setEventTime(eventTime);
        assetHistoryRepository.save(history);
    }

    private AssetHistoryResponse toResponse(AssetHistory history) {
        AssetHistoryResponse response = new AssetHistoryResponse();
        response.setId(history.getId());
        response.setAssetId(history.getAssetId());
        response.setAction(history.getAction());
        response.setPreviousStatus(history.getPreviousStatus());
        response.setNewStatus(history.getNewStatus());
        response.setPreviousValue(history.getPreviousValue());
        response.setNewValue(history.getNewValue());
        response.setRemarks(history.getRemarks());
        response.setPerformedByUserId(history.getPerformedByUserId());
        response.setEventTime(history.getEventTime());
        return response;
    }
}

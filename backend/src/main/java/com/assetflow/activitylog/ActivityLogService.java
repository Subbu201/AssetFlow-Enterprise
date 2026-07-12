package com.assetflow.activitylog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void log(Long actorUserId, String action, String entityType, Long entityId, String description) {
        ActivityLog log = new ActivityLog();
        log.setActorUserId(actorUserId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setActionTime(LocalDateTime.now());
        repository.save(log);
    }

    public Page<ActivityLog> search(Specification<ActivityLog> specification, Pageable pageable) {
        return repository.findAll(specification, pageable);
    }
}

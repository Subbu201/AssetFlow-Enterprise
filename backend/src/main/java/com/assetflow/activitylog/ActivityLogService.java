package com.assetflow.activitylog;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<ActivityLog> search(Specification<ActivityLog> specification) {
        return repository.findAll(specification);
    }
}

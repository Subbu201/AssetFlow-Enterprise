package com.assetflow.activitylog;

import com.assetflow.activitylog.dto.ActivityLogFilterRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ActivityLogSpecification {

    public static Specification<ActivityLog> filter(ActivityLogFilterRequest request) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (request.getActorUserId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("actorUserId"), request.getActorUserId()));
            }
            if (request.getAction() != null && !request.getAction().isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("action"), request.getAction()));
            }
            if (request.getEntityType() != null && !request.getEntityType().isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("entityType"), request.getEntityType()));
            }
            if (request.getDateFrom() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("actionTime"), request.getDateFrom().atStartOfDay()));
            }
            if (request.getDateTo() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("actionTime"), request.getDateTo().atTime(23, 59, 59)));
            }

            return predicates;
        };
    }
}

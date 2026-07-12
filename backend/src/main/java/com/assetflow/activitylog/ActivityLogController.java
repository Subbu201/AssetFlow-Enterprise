package com.assetflow.activitylog;

import com.assetflow.activitylog.dto.ActivityLogFilterRequest;
import com.assetflow.activitylog.dto.ActivityLogResponse;
import com.assetflow.common.ApiResponse;
import com.assetflow.exception.ForbiddenException;
import com.assetflow.exception.UnauthorizedException;
import com.assetflow.common.Role;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/activity-logs")
public class ActivityLogController {

    private final ActivityLogService service;

    public ActivityLogController(ActivityLogService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<ActivityLogResponse>> getActivityLogs(ActivityLogFilterRequest filterRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name()))) {
            throw new ForbiddenException("Access denied");
        }

        Specification<ActivityLog> specification = buildSpecification(filterRequest);
        List<ActivityLogResponse> logs = service.search(specification).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Activity logs returned", logs);
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .actorUserId(log.getActorUserId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .actionTime(log.getActionTime())
                .build();
    }

    private Specification<ActivityLog> buildSpecification(ActivityLogFilterRequest request) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (request.getActorUserId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("actorUserId"), request.getActorUserId()));
            }
            if (request.getAction() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("action"), request.getAction()));
            }
            if (request.getEntityType() != null) {
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

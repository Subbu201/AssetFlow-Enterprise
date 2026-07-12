package com.assetflow.activitylog;

import com.assetflow.activitylog.dto.ActivityLogFilterRequest;
import com.assetflow.activitylog.dto.ActivityLogPageResponse;
import com.assetflow.activitylog.dto.ActivityLogResponse;
import com.assetflow.common.ApiResponse;
import com.assetflow.exception.ForbiddenException;
import com.assetflow.exception.UnauthorizedException;
import com.assetflow.common.Role;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/admin/activity-logs")
public class ActivityLogController {

    private final ActivityLogService service;

    public ActivityLogController(ActivityLogService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<ActivityLogPageResponse> getActivityLogs(@Valid ActivityLogFilterRequest filterRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name()))) {
            throw new ForbiddenException("Access denied");
        }

        Specification<ActivityLog> specification = ActivityLogSpecification.filter(filterRequest);
        PageRequest pageRequest = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), buildSort(filterRequest.getSort()));
        Page<ActivityLog> page = service.search(specification, pageRequest);

        List<ActivityLogResponse> logs = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        ActivityLogPageResponse response = ActivityLogPageResponse.builder()
                .logs(logs)
                .totalElements(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .build();

        return ApiResponse.success("Activity logs returned", response);
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

    private Sort buildSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "actionTime");
        }

        String[] parts = sort.split(",");
        if (parts.length != 2) {
            return Sort.by(Sort.Direction.DESC, "actionTime");
        }

        String property = parts[0].trim();
        String direction = parts[1].trim();
        try {
            return Sort.by(Sort.Direction.fromString(direction), property);
        } catch (IllegalArgumentException e) {
            return Sort.by(Sort.Direction.DESC, "actionTime");
        }
    }
}

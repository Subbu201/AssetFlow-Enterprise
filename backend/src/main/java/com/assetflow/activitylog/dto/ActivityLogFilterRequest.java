package com.assetflow.activitylog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ActivityLogFilterRequest {
    private Long actorUserId;
    private String action;
    private String entityType;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    @Min(value = 0, message = "Page index must be 0 or greater")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size = 20;

    @Pattern(regexp = "^[a-zA-Z0-9_]+,(asc|desc)$", message = "Sort must be provided as field,direction")
    private String sort = "actionTime,desc";
}

package com.assetflow.activitylog.dto;

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
    private Integer page;
    private Integer size;
    private String sort;
}

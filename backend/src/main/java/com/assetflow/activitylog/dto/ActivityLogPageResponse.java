package com.assetflow.activitylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogPageResponse {
    private List<ActivityLogResponse> logs;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}

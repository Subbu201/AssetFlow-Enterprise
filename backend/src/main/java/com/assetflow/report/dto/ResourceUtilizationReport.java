package com.assetflow.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUtilizationReport {
    private String resourceType;
    private long usedCount;
    private long totalCount;
}

package com.assetflow.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceFrequencyReport {
    private Long maintenanceRequestId;
    private String assetName;
    private long maintenanceCount;
}

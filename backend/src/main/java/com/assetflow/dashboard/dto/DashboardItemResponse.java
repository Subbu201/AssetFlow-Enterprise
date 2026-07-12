package com.assetflow.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardItemResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String details;
    private String date;
}

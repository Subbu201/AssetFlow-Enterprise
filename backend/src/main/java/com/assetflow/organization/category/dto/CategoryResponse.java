package com.assetflow.organization.category.dto;

import com.assetflow.common.RecordStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer warrantyPeriodMonths;
    private RecordStatus status;
}

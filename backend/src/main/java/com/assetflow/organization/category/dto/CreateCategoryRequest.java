package com.assetflow.organization.category.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Code is required")
    private String code;

    private String description;

    @Min(value = 0, message = "Warranty period cannot be negative")
    private Integer warrantyPeriodMonths;
}

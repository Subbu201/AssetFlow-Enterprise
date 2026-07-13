package com.assetflow.asset.dto;

import com.assetflow.common.AssetStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateAssetRequest {
    @NotBlank
    private String name;

    @NotNull
    private Long categoryId;

    private String serialNumber;

    private LocalDate acquisitionDate;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal acquisitionCost;

    private String condition;

    private String location;

    private Long departmentId;

    private boolean sharedBookable;

    private String photoUrl;

    private String documentUrl;

    private String manufacturer;

    private String model;

    private LocalDate warrantyExpiryDate;

    private String notes;

    private Long registeredByUserId;

    private Integer quantity;
}

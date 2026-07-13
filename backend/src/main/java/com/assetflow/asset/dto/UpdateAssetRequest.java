package com.assetflow.asset.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateAssetRequest {
    private String name;
    private Long categoryId;
    private String serialNumber;
    private LocalDate acquisitionDate;
    private BigDecimal acquisitionCost;
    private String condition;
    private String location;
    private Long departmentId;
    private Boolean sharedBookable;
    private String photoUrl;
    private String documentUrl;
    private String manufacturer;
    private String model;
    private LocalDate warrantyExpiryDate;
    private String notes;
    private Integer quantity;
}

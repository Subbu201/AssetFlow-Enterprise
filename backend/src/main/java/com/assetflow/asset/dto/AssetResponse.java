package com.assetflow.asset.dto;

import com.assetflow.common.AssetStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AssetResponse {
    private Long id;
    private String assetTag;
    private String name;
    private Long categoryId;
    private String serialNumber;
    private LocalDate acquisitionDate;
    private BigDecimal acquisitionCost;
    private String condition;
    private String location;
    private Long departmentId;
    private boolean sharedBookable;
    private AssetStatus status;
    private String photoUrl;
    private String documentUrl;
    private String manufacturer;
    private String model;
    private LocalDate warrantyExpiryDate;
    private String notes;
    private Long registeredByUserId;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.assetflow.asset;

import com.assetflow.common.AssetStatus;
import com.assetflow.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "assets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_asset_tag", columnNames = "assetTag"),
        @UniqueConstraint(name = "uk_asset_serial", columnNames = "serialNumber")
})
@Getter
@Setter
public class Asset extends BaseEntity {

    @Column(nullable = false, unique = true, updatable = false)
    private String assetTag;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long categoryId;

    @Column(unique = true)
    private String serialNumber;

    private LocalDate acquisitionDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal acquisitionCost;

    private String condition;

    private String location;

    private Long departmentId;

    private boolean sharedBookable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;

    private String photoUrl;

    private String documentUrl;

    private String manufacturer;

    private String model;

    private LocalDate warrantyExpiryDate;

    @Column(length = 1000)
    private String notes;

    private Long registeredByUserId;

    @Version
    private Long version;

    @PrePersist
    private void prePersist() {
        if (status == null) {
            status = AssetStatus.AVAILABLE;
        }
    }
}

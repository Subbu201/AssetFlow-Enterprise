package com.assetflow.allocation;

import com.assetflow.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "asset_allocations")
@Getter
@Setter
public class AssetAllocation extends BaseEntity {

    private Long assetId;

    private Long employeeId;

    private Long departmentId;

    private Long allocatedByUserId;

    private LocalDate allocationDate;

    private LocalDate expectedReturnDate;

    private LocalDate actualReturnDate;

    @Column(nullable = false)
    private String status;

    private String checkoutCondition;

    private String notes;

    @Version
    private Long version;
}

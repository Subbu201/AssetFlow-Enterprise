package com.assetflow.transfer;

import com.assetflow.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_requests")
@Getter
@Setter
public class TransferRequest extends BaseEntity {

    private Long allocationId;
    private Long assetId;
    private Long requestedByUserId;
    private Long targetEmployeeId;
    private Long targetDepartmentId;
    private String reason;
    private String status;
    private Long reviewedByUserId;
    private LocalDateTime reviewedAt;
    private String reviewComments;
}

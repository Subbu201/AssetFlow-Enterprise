package com.assetflow.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditCycleResponse {
    private Long id;
    private String auditCode;
    private String name;
    private String scopeType;
    private Long departmentId;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long createdByUserId;
    private Long closedByUserId;
    private LocalDateTime closedAt;
}

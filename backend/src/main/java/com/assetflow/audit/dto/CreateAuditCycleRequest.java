package com.assetflow.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreateAuditCycleRequest {

    @NotBlank
    private String auditCode;

    @NotBlank
    private String name;

    @NotBlank
    private String scopeType;

    private Long departmentId;

    private String location;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}

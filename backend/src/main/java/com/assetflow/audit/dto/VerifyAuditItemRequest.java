package com.assetflow.audit.dto;

import com.assetflow.common.AuditVerificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerifyAuditItemRequest {

    @NotNull
    private AuditVerificationStatus verificationStatus;

    private String actualLocation;

    private String actualCondition;

    private String remarks;
}

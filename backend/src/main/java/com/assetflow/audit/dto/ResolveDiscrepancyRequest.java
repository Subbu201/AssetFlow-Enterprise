package com.assetflow.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResolveDiscrepancyRequest {

    @NotNull
    private Long resolvedByUserId;

    @NotBlank
    private String resolutionNotes;
}
